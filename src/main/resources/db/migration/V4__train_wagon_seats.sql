-- 540 мест = 15 купейных вагонов (36×15) = 10 плацкартных (54×10) = 30 СВ (18×30)
UPDATE routes
SET total_seats     = 540,
    available_seats = 540
WHERE transport_type = 'train';
