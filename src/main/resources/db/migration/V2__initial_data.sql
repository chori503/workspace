-- Insert de usuarios iniciales
-- Contraseña:123456
-- Admin
INSERT INTO public.app_user (email,"password",full_name,"role")
VALUES ('jorgearomero123@gmail.com','$2a$10$p2ZIu07bPUHBN/uA3bUilupeZn1GQqfox7LsEQM3QKZl1wfRYb/Sa','Jorge Romero','ADMIN');
INSERT INTO public.app_user (email,"password",full_name,"role")
VALUES ('andrea@gmail.com','$2a$10$p2ZIu07bPUHBN/uA3bUilupeZn1GQqfox7LsEQM3QKZl1wfRYb/Sa','Andrea Hernández','ADMIN');

--User
INSERT INTO public.app_user (email,"password",full_name,"role")
VALUES ('albertoguzman@gmail.com','$2a$10$p2ZIu07bPUHBN/uA3bUilupeZn1GQqfox7LsEQM3QKZl1wfRYb/Sa','Alberto Guzman','USER');
INSERT INTO public.app_user (email,"password",full_name,"role")
VALUES ('juan@gmail.com','$2a$10$p2ZIu07bPUHBN/uA3bUilupeZn1GQqfox7LsEQM3QKZl1wfRYb/Sa','Juan Perez','USER');

-- Insert de espacios iniciales
INSERT INTO public.space (name, type, capacity, location, floor, hourly_rate, status)
VALUES ('Cubículo A1', 'OPEN_DESK', 1, 'Área Norte', 'Piso 1', 3.50, 'ACTIVE');
INSERT INTO public.space (name, type, capacity, location, floor, hourly_rate, status)
VALUES ('Cubículo A2', 'OPEN_DESK', 1, 'Área Norte', 'Piso 1', 3.50, 'ACTIVE');
INSERT INTO public.space (name, type, capacity, location, floor, hourly_rate, status)
VALUES ('Cubículo B1', 'OPEN_DESK', 1, 'Área Sur', 'Piso 1', 3.00, 'ACTIVE');
INSERT INTO public.space (name, type, capacity, location, floor, hourly_rate, status)
VALUES ('Cubículo C1', 'OPEN_DESK', 1, 'Área Sur', 'Piso 1', 3.00, 'INACTIVE');
INSERT INTO public.space (name, type, capacity, location, floor, hourly_rate, status)
VALUES ('Sala Volcán', 'MEETING_ROOM', 6, 'Ala Este', 'Piso 2', 15.00, 'ACTIVE');
INSERT INTO public.space (name, type, capacity, location, floor, hourly_rate, status)
VALUES ('Sala Izalco', 'MEETING_ROOM', 8, 'Ala Este', 'Piso 2', 18.00, 'ACTIVE');
INSERT INTO public.space (name, type, capacity, location, floor, hourly_rate, status)
VALUES ('Sala Coatepeque', 'MEETING_ROOM', 4, 'Ala Oeste', 'Piso 2', 12.00, 'ACTIVE');
INSERT INTO public.space (name, type, capacity, location, floor, hourly_rate, status)
VALUES ('Sala Ilamatepec', 'MEETING_ROOM', 10, 'Ala Oeste', 'Piso 3', 22.00, 'ACTIVE');
INSERT INTO public.space (name, type, capacity, location, floor, hourly_rate, status)
VALUES ('Sala Santa Ana', 'MEETING_ROOM', 12, 'Ala Este', 'Piso 4', 25.00, 'ACTIVE');
INSERT INTO public.space (name, type, capacity, location, floor, hourly_rate, status)
VALUES ('Oficina Privada Ceiba', 'PRIVATE_OFFICE', 2, 'Torre Norte', 'Piso 3', 10.00, 'ACTIVE');
INSERT INTO public.space (name, type, capacity, location, floor, hourly_rate, status)
VALUES ('Oficina Privada Maquilishuat', 'PRIVATE_OFFICE', 3, 'Torre Norte', 'Piso 3', 14.00, 'ACTIVE');
INSERT INTO public.space (name, type, capacity, location, floor, hourly_rate, status)
VALUES ('Oficina Privada Conacaste', 'PRIVATE_OFFICE', 1, 'Torre Sur', 'Piso 4', 9.00, 'ACTIVE');
