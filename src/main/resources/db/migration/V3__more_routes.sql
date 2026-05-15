-- Самолёты (150 мест, 6 колонн)
INSERT INTO routes (origin_city, destination_city, departure_time, arrival_time, price, total_seats, available_seats, transport_type) VALUES
('Москва',          'Сочи',             NOW() + INTERVAL '1 day 8 hours',   NOW() + INTERVAL '1 day 10 hours 30 min', 4500.00, 150, 150, 'plane'),
('Москва',          'Екатеринбург',     NOW() + INTERVAL '2 days 6 hours',   NOW() + INTERVAL '2 days 8 hours',        3800.00, 150, 148, 'plane'),
('Санкт-Петербург', 'Сочи',             NOW() + INTERVAL '3 days 7 hours',   NOW() + INTERVAL '3 days 9 hours 20 min', 5200.00, 150, 142, 'plane'),
('Москва',          'Новосибирск',      NOW() + INTERVAL '1 day 10 hours',   NOW() + INTERVAL '1 day 14 hours',        6100.00, 180, 180, 'plane'),
('Екатеринбург',    'Москва',           NOW() + INTERVAL '4 days 9 hours',   NOW() + INTERVAL '4 days 11 hours',       3900.00, 150, 130, 'plane'),
('Москва',          'Калининград',      NOW() + INTERVAL '2 days 12 hours',  NOW() + INTERVAL '2 days 14 hours',       4200.00, 130, 130, 'plane'),
('Сочи',            'Санкт-Петербург',  NOW() + INTERVAL '5 days 8 hours',   NOW() + INTERVAL '5 days 10 hours 30 min',5000.00, 150, 150, 'plane');

-- Поезда (54 места, 4 колонны)
INSERT INTO routes (origin_city, destination_city, departure_time, arrival_time, price, total_seats, available_seats, transport_type) VALUES
('Москва',          'Воронеж',          NOW() + INTERVAL '1 day 14 hours',   NOW() + INTERVAL '1 day 20 hours',        1200.00, 54, 54, 'train'),
('Москва',          'Ростов-на-Дону',   NOW() + INTERVAL '2 days 18 hours',  NOW() + INTERVAL '3 days 6 hours',        2600.00, 54, 50, 'train'),
('Санкт-Петербург', 'Москва',           NOW() + INTERVAL '1 day 22 hours',   NOW() + INTERVAL '2 days 4 hours 30 min', 1800.00, 72, 70, 'train'),
('Москва',          'Омск',             NOW() + INTERVAL '3 days 20 hours',  NOW() + INTERVAL '5 days 4 hours',        4100.00, 54, 54, 'train'),
('Казань',          'Санкт-Петербург',  NOW() + INTERVAL '4 days 17 hours',  NOW() + INTERVAL '5 days 9 hours',        3500.00, 54, 40, 'train'),
('Ростов-на-Дону',  'Москва',           NOW() + INTERVAL '6 days 19 hours',  NOW() + INTERVAL '7 days 7 hours',        2700.00, 54, 54, 'train');
