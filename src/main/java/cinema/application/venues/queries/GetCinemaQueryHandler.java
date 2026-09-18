package cinema.application.venues.queries;

import cinema.application.common.exceptions.NotFoundException;
import cinema.application.common.exceptions.ValidationException;
import cinema.application.common.interfaces.CinemaRepository;
import cinema.application.common.interfaces.UseCase;
import cinema.application.venues.models.CinemaView;

public final class GetCinemaQueryHandler implements UseCase<GetCinemaQuery, CinemaView> {

    private final CinemaRepository cinemas;

    public GetCinemaQueryHandler(CinemaRepository cinemas) {
        this.cinemas = cinemas;
    }

    @Override
    public CinemaView handle(GetCinemaQuery input) {
        if (input == null || input.id() == null || input.id().isBlank()) {
            throw new ValidationException("Cinema ID must not be blank");
        }
        var cinema = cinemas.findById(input.id()).orElseThrow(() -> new NotFoundException("Cinema not found: " + input.id()));
        return new CinemaView(cinema.getId(), cinema.getName(), cinema.getAddress());
    }
}
