-- V10: Связь admins → routes
-- Каждый маршрут хранит ID администратора, который его создал.
-- ON DELETE SET NULL: если администратор удалён, маршруты остаются (исторические данные).

ALTER TABLE routes
    ADD COLUMN created_by_admin_id INT
        REFERENCES admins(id) ON DELETE SET NULL;

CREATE INDEX idx_routes_admin ON routes(created_by_admin_id);
