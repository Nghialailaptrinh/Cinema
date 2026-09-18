package cinema.application.venues.queries;

import cinema.application.common.exceptions.NotFoundException;
import cinema.application.common.exceptions.ValidationException;
import cinema.application.common.interfaces.HallRepository;
import cinema.application.common.interfaces.UseCase;
import cinema.application.venues.models.HallView;

public final class GetHallQueryHandler implements UseCase<GetHallQuery, HallView> {

    private final HallRepository halls;

    public GetHallQueryHandler(HallRepository halls) {
        this.halls = halls;
    }

    @Override
    public HallView handle(GetHallQuery input) {
        if (input == null || input.id() == null || input.id().isBlank()) {
            throw new ValidationException("Hall ID must not be blank");
        }
        var hall = halls.findById(input.id()).orElseThrow(() -> new NotFoundException("Hall not found: " + input.id()));
        return new HallView(hall.getId(), hall.getCinemaId(), hall.getName());
    }
}
