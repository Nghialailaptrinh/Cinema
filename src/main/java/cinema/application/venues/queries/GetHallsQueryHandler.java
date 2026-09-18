package cinema.application.venues.queries;

import java.util.List;

import cinema.application.common.exceptions.NotFoundException;
import cinema.application.common.exceptions.ValidationException;
import cinema.application.common.interfaces.CinemaRepository;
import cinema.application.common.interfaces.HallRepository;
import cinema.application.common.interfaces.UseCase;
import cinema.application.venues.models.HallView;

public final class GetHallsQueryHandler implements UseCase<GetHallsQuery, List<HallView>> {

    private final CinemaRepository cinemas;
    private final HallRepository halls;

    public GetHallsQueryHandler(CinemaRepository cinemas, HallRepository halls) {
        this.cinemas = cinemas;
        this.halls = halls;
    }

    @Override
    public List<HallView> handle(GetHallsQuery input) {
        if (input == null || input.cinemaId() == null || input.cinemaId().isBlank()) {
            throw new ValidationException("Cinema ID must not be blank");
        }
        cinemas.findById(input.cinemaId()).orElseThrow(() -> new NotFoundException("Cinema not found: " + input.cinemaId()));
        return halls.findByCinemaId(input.cinemaId()).stream().map(hall
                -> new HallView(hall.getId(), hall.getCinemaId(), hall.getName())).toList();
    }
}
