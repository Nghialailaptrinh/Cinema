package cinema.infrastructure.persistence.inmemory;

import cinema.application.common.exceptions.ConflictException;
import cinema.domain.cinema.*;
import java.util.List;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import static org.junit.jupiter.api.Assertions.*;

class VenueRepositoryContractTest {
    @Test
    void missingIdsAndParentsReturnEmptyResults() {
        var cinemas = new InMemoryCinemaRepository();
        var halls = new InMemoryHallRepository();
        var seats = new InMemorySeatRepository();
        assertTrue(cinemas.findById("missing").isEmpty());
        assertTrue(halls.findById("missing").isEmpty());
        assertTrue(seats.findById("missing").isEmpty());
        assertTrue(cinemas.findAll().isEmpty());
        assertTrue(halls.findByCinemaId("missing").isEmpty());
        assertTrue(seats.findByHallId("missing").isEmpty());
        assertFalse(halls.existsByCinemaIdAndName("missing", "Hall"));
        assertFalse(seats.existsByHallIdAndRowAndNumber("missing", "A", 1));
    }

    @Test
    void duplicateIdsNeverOverwriteExistingEntitiesOrReserveNewKeys() {
        var cinemas = new InMemoryCinemaRepository();
        var halls = new InMemoryHallRepository();
        var seats = new InMemorySeatRepository();
        var cinema = new Cinema("c1", "Original", "Address");
        var hall = new Hall("h1", "c1", "Original");
        var seat = new Seat("s1", "h1", "A", 1, SeatType.NORMAL);
        cinemas.save(cinema);
        halls.save(hall);
        seats.save(seat);
        assertThrows(ConflictException.class, () -> cinemas.save(new Cinema("c1", "Replacement", "Elsewhere")));
        assertThrows(ConflictException.class, () -> halls.save(new Hall("h1", "c2", "Other")));
        assertThrows(ConflictException.class, () -> seats.save(new Seat("s1", "h2", "B", 2, SeatType.VIP)));
        assertSame(cinema, cinemas.findById("c1").orElseThrow());
        assertSame(hall, halls.findById("h1").orElseThrow());
        assertSame(seat, seats.findById("s1").orElseThrow());
        assertFalse(halls.existsByCinemaIdAndName("c2", "Other"));
        assertFalse(seats.existsByHallIdAndRowAndNumber("h2", "B", 2));
        halls.save(new Hall("h2", "c2", "Other"));
        seats.save(new Seat("s2", "h2", "B", 2, SeatType.VIP));
    }

    @Test
    void businessKeyConflictsDoNotInsertTheLosingId() {
        var halls = new InMemoryHallRepository();
        var seats = new InMemorySeatRepository();
        halls.save(new Hall("h1", "c1", "Hall"));
        seats.save(new Seat("s1", "h1", "A", 1, SeatType.NORMAL));
        assertThrows(ConflictException.class, () -> halls.save(new Hall("h2", "c1", " Hall ")));
        assertThrows(ConflictException.class, () -> seats.save(new Seat("s2", "h1", " a ", 1, SeatType.VIP)));
        assertTrue(halls.findById("h2").isEmpty());
        assertTrue(seats.findById("s2").isEmpty());
        assertEquals(1, halls.findByCinemaId("c1").size());
        assertEquals(1, seats.findByHallId("h1").size());
    }

    @Test
    void snapshotsAreImmutableAndUnaffectedByLaterInserts() {
        var cinemas = new InMemoryCinemaRepository();
        var halls = new InMemoryHallRepository();
        var seats = new InMemorySeatRepository();
        cinemas.save(new Cinema("c1", "Cinema", "Address"));
        halls.save(new Hall("h1", "c1", "Hall"));
        seats.save(new Seat("s1", "h1", "A", 1, SeatType.NORMAL));
        var cinemaSnapshot = cinemas.findAll();
        var hallSnapshot = halls.findByCinemaId("c1");
        var seatSnapshot = seats.findByHallId("h1");
        assertThrows(UnsupportedOperationException.class, cinemaSnapshot::clear);
        assertThrows(UnsupportedOperationException.class, hallSnapshot::clear);
        assertThrows(UnsupportedOperationException.class, seatSnapshot::clear);
        cinemas.save(new Cinema("c2", "Cinema", "Address"));
        halls.save(new Hall("h2", "c1", "Second"));
        seats.save(new Seat("s2", "h1", "A", 2, SeatType.NORMAL));
        assertEquals(1, cinemaSnapshot.size());
        assertEquals(1, hallSnapshot.size());
        assertEquals(1, seatSnapshot.size());
        assertEquals(2, cinemas.findAll().size());
        assertEquals(2, halls.findByCinemaId("c1").size());
        assertEquals(2, seats.findByHallId("h1").size());
        assertTrue(new InMemoryCinemaRepository().findAll().isEmpty());
        assertTrue(new InMemoryHallRepository().findByCinemaId("c1").isEmpty());
        assertTrue(new InMemorySeatRepository().findByHallId("h1").isEmpty());
    }

    @Test
    void hallNamesAreCaseSensitiveAndSeatNumbersSortNumerically() {
        var halls = new InMemoryHallRepository();
        halls.save(new Hall("h1", "c1", "Hall"));
        halls.save(new Hall("h2", "c1", "hall"));
        assertEquals(2, halls.findByCinemaId("c1").size());
        var seats = new InMemorySeatRepository();
        seats.save(new Seat("s10", "h1", "A", 10, SeatType.NORMAL));
        seats.save(new Seat("s2", "h1", "A", 2, SeatType.NORMAL));
        seats.save(new Seat("sB", "h1", "B", 1, SeatType.NORMAL));
        assertEquals(List.of("s2", "s10", "sB"), seats.findByHallId("h1").stream().map(Seat::getId).toList());
    }

    @Test
    @Timeout(10)
    void concurrentDuplicateHallNameAllowsExactlyOneInsert() throws Exception {
        var repository = new InMemoryHallRepository();
        assertOneWinner(() -> repository.save(new Hall("h1", "c1", "Hall")),
                () -> repository.save(new Hall("h2", "c1", " Hall ")));
        assertEquals(1, repository.findByCinemaId("c1").size());
        assertTrue(repository.existsByCinemaIdAndName("c1", "Hall"));
        assertNotEquals(repository.findById("h1").isPresent(), repository.findById("h2").isPresent());
    }

    @Test
    @Timeout(10)
    void concurrentDuplicateSeatPositionAllowsExactlyOneInsert() throws Exception {
        var repository = new InMemorySeatRepository();
        assertOneWinner(() -> repository.save(new Seat("s1", "h1", "a", 1, SeatType.NORMAL)),
                () -> repository.save(new Seat("s2", "h1", " A ", 1, SeatType.VIP)));
        assertEquals(1, repository.findByHallId("h1").size());
        assertTrue(repository.existsByHallIdAndRowAndNumber("h1", "A", 1));
        assertNotEquals(repository.findById("s1").isPresent(), repository.findById("s2").isPresent());
    }

    private static void assertOneWinner(Runnable first, Runnable second) throws Exception {
        var executor = Executors.newFixedThreadPool(2);
        var ready = new CountDownLatch(2);
        var start = new CountDownLatch(1);
        try {
            var futures = List.of(executor.submit(atStart(first, ready, start)),
                    executor.submit(atStart(second, ready, start)));
            assertTrue(ready.await(2, TimeUnit.SECONDS), "Both writers must be ready");
            start.countDown();
            int successes = 0;
            int conflicts = 0;
            for (var future : futures) {
                if (future.get(3, TimeUnit.SECONDS)) successes++;
                else conflicts++;
            }
            assertEquals(1, successes);
            assertEquals(1, conflicts);
        } finally {
            start.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(2, TimeUnit.SECONDS), "Writers must stop");
        }
    }

    private static Callable<Boolean> atStart(Runnable insert, CountDownLatch ready, CountDownLatch start) {
        return () -> {
            ready.countDown();
            if (!start.await(2, TimeUnit.SECONDS)) throw new TimeoutException("Start gate timed out");
            try {
                insert.run();
                return true;
            } catch (ConflictException expected) {
                return false;
            }
        };
    }
}
