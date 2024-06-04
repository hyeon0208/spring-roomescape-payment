package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.dto.LoginMember;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.dto.AdminReservationSaveRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.ReservationSearchConditionRequest;
import roomescape.reservation.dto.ReservationWaitingResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.repository.ThemeRepository;

@Service
@Transactional
public class AdminReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public AdminReservationService(
            ReservationRepository reservationRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository,
            MemberRepository memberRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public ReservationResponse save(
            AdminReservationSaveRequest adminReservationSaveRequest,
            LoginMember loginMember
    ) {
        ReservationTime reservationTime = reservationTimeRepository.findById(adminReservationSaveRequest.timeId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약 시간입니다."));

        Theme theme = themeRepository.findById(adminReservationSaveRequest.themeId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 테마입니다."));

        Member member = memberRepository.findById(loginMember.id())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        Reservation reservation = adminReservationSaveRequest.toEntity(member, theme, reservationTime, ReservationStatus.SUCCESS);
        if (reservationRepository.existsByDateAndTimeStartAtAndStatus(
                reservation.getDate(),
                reservation.getStartAt(),
                reservation.getStatus()
        )) {
            throw new IllegalArgumentException("중복된 예약이 있습니다.");
        }

        Reservation savedReservation = reservationRepository.save(reservation);

        return ReservationResponse.toResponse(savedReservation);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findAllBySearchCondition(ReservationSearchConditionRequest request) {
        return reservationRepository.findAllByThemeIdAndMemberIdAndDateBetweenOrderByDateAscTimeStartAtAscCreatedAtAsc(
                        request.themeId(),
                        request.memberId(),
                        request.dateFrom(),
                        request.dateTo()
                ).stream()
                .map(ReservationResponse::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationWaitingResponse> findWaitingReservations() {
        List<Reservation> waitingReservations = reservationRepository.findAllByStatusFromDate(ReservationStatus.WAIT,
                LocalDate.now());

        return waitingReservations.stream()
                .map(ReservationWaitingResponse::toResponse)
                .toList();
    }
}
