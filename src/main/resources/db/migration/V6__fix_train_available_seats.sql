UPDATE routes
SET total_seats     = 654,
    available_seats = 654 - COALESCE(
        (SELECT SUM(t.seat_count)
         FROM tickets t
         WHERE t.route_id = routes.id
           AND t.status   = 'active'),
        0
    )
WHERE transport_type = 'train';
