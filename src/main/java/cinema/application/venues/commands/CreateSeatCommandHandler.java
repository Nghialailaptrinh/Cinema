package cinema.application.venues.commands;

import java.util.function.Supplier;

import cinema.application.common.exceptions.ConflictException;
import cinema.application.common.exceptions.NotFoundException;
import cinema.application.common.exceptions.ValidationException;
import cinema.application.common.interfaces.HallRepository;
import cinema.application.common.interfaces.SeatRepository;
import cinema.application.common.interfaces.UseCase;
import cinema.domain.cinema.Seat;
import cinema.domain.cinema.SeatType;
import cinema.domain.common.DomainException;

public final class CreateSeatCommandHandler implements UseCase<CreateSeatCommand, String> {

    private final HallRepository halls;
    private final SeatRepository seats;
    private final Supplier<String> idGenerator;

    public CreateSeatCommandHandler(HallRepository halls, SeatRepository seats, Supplier<String> idGenerator) {
        this.halls = halls;
        this.seats = seats;
        this.idGenerator = idGenerator;
    }

    @Override
    public String handle(CreateSeatCommand input) {
        if (input == null || input.hallId() == null || input.hallId().isBlank()) {
            throw new ValidationException("Hall ID must not be blank");
        }
        if (input.number() == null || input.number() <= 0) {
            throw new ValidationException("Seat number must be positive");
        }
        SeatType type;
        try {
            type = input.type() == null ? null : SeatType.valueOf(input.type());
        } catch (IllegalArgumentException exception) {
            throw new ValidationException("Unknown seat type");
        }
        if (type == null) {
            throw new ValidationException("Seat type must not be null");
        }
        halls.findById(input.hallId())
                .orElseThrow(() -> new NotFoundException("Hall not found: " + input.hallId()));
        try {
            Seat seat = new Seat(idGenerator.get(), input.hallId(), input.row(), input.number(), type);
            if (seats.existsByHallIdAndRowAndNumber(seat.getHallId(), seat.getRow(), seat.getNumber())) {
                throw new ConflictException("Seat position already exists in hall");
            }
            seats.save(seat);
            return seat.getId();
        } catch (DomainException exception) {
            throw new ValidationException(exception.getMessage());
        }
    }
}
