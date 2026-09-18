package cinema.application.venues.queries;

import java.util.List;

import cinema.application.common.exceptions.NotFoundException;
import cinema.application.common.exceptions.ValidationException;
import cinema.application.common.interfaces.HallRepository;
import cinema.application.common.interfaces.SeatRepository;
import cinema.application.common.interfaces.UseCase;
import cinema.application.venues.models.PhysicalSeatView;

public final class GetSeatsQueryHandler implements UseCase<GetSeatsQuery, List<PhysicalSeatView>> {

    private final HallRepository halls;
    private final SeatRepository seats;

    public GetSeatsQueryHandler(HallRepository halls, SeatRepository seats) {
        this.halls = halls;
        this.seats = seats;
    }

    @Override
    public List<PhysicalSeatView> handle(GetSeatsQuery input) {
        if (input == null || input.hallId() == null || input.hallId().isBlank()) {
            throw new ValidationException("Hall ID must not be blank");
        }
        halls.findById(input.hallId()).orElseThrow(() -> new NotFoundException("Hall not found: " + input.hallId()));
        return seats.findByHallId(input.hallId()).stream().map(seat
                -> new PhysicalSeatView(seat.getId(), seat.getHallId(), seat.getRow(), seat.getNumber(), seat.getType().name())).toList();
    }
}
