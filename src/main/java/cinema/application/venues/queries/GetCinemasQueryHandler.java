package cinema.application.venues.queries;

import java.util.List;

import cinema.application.common.exceptions.ValidationException;
import cinema.application.common.interfaces.CinemaRepository;
import cinema.application.common.interfaces.UseCase;
import cinema.application.venues.models.CinemaView;

public final class GetCinemasQueryHandler implements UseCase<GetCinemasQuery, List<CinemaView>> {

    private final CinemaRepository cinemas;

    public GetCinemasQueryHandler(CinemaRepository cinemas) {
        this.cinemas = cinemas;
    }

    @Override
    public List<CinemaView> handle(GetCinemasQuery input) {
        if (input == null) {
            throw new ValidationException("Query must not be null");
        }
        return cinemas.findAll().stream().map(cinema
                -> new CinemaView(cinema.getId(), cinema.getName(), cinema.getAddress())).toList();
    }
}
