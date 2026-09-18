package cinema.application.venues.commands;

import java.util.function.Supplier;

import cinema.application.common.exceptions.NotFoundException;
import cinema.application.common.exceptions.ValidationException;
import cinema.application.common.interfaces.CinemaRepository;
import cinema.application.common.interfaces.HallRepository;
import cinema.application.common.interfaces.UseCase;
import cinema.domain.cinema.Hall;
import cinema.domain.common.DomainException;

public final class CreateHallCommandHandler implements UseCase<CreateHallCommand, String> {

    private final CinemaRepository cinemas;
    private final HallRepository halls;
    private final Supplier<String> idGenerator;

    public CreateHallCommandHandler(CinemaRepository cinemas, HallRepository halls, Supplier<String> idGenerator) {
        this.cinemas = cinemas;
        this.halls = halls;
        this.idGenerator = idGenerator;
    }

    @Override
    public String handle(CreateHallCommand input) {
        if (input == null || input.cinemaId() == null || input.cinemaId().isBlank()) {
            throw new ValidationException("Cinema ID must not be blank");
        }
        if (input.name() == null || input.name().isBlank()) {
            throw new ValidationException("Hall name must not be blank");
        }
        cinemas.findById(input.cinemaId())
                .orElseThrow(() -> new NotFoundException("Cinema not found: " + input.cinemaId()));
        try {
            Hall hall = new Hall(idGenerator.get(), input.cinemaId(), input.name());
            if (halls.existsByCinemaIdAndName(hall.getCinemaId(), hall.getName())) {
                throw new cinema.application.common.exceptions.ConflictException("Hall name already exists in cinema");
            }
            halls.save(hall);
            return hall.getId();
        } catch (DomainException exception) {
            throw new ValidationException(exception.getMessage());
        }
    }
}
