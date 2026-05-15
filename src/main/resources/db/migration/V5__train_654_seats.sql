-- 654 места = 1 СВ (18) + 4 Купе (144) + 8 Плацкарт (432) + 1 Сидячий (60) = 14 вагонов
UPDATE routes
SET total_seats     = 654,
    available_seats = 654
WHERE transport_type = 'train';
