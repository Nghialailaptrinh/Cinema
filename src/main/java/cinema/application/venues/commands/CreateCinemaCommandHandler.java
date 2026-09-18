package cinema.application.venues.commands;

import java.util.function.Supplier;

import cinema.application.common.exceptions.ValidationException;
import cinema.application.common.interfaces.CinemaRepository;
import cinema.application.common.interfaces.UseCase;
import cinema.domain.cinema.Cinema;
import cinema.domain.common.DomainException;

public final class CreateCinemaCommandHandler implements UseCase<CreateCinemaCommand, String> {

    private final CinemaRepository cinemas;
    private final Supplier<String> idGenerator;

    public CreateCinemaCommandHandler(CinemaRepository cinemas, Supplier<String> idGenerator) {
        this.cinemas = cinemas;
        this.idGenerator = idGenerator;
    }

    @Override
    public String handle(CreateCinemaCommand input) {
        if (input == null) {
            throw new ValidationException("Create cinema command must not be null");
        }
        try {
            Cinema cinema = new Cinema(idGenerator.get(), input.name(), input.address());
            cinemas.save(cinema);
            return cinema.getId();
        } catch (DomainException exception) {
            throw new ValidationException(exception.getMessage());
        }
    }
}
