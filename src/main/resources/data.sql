-- Insert into roles
INSERT INTO roles (role_name) VALUES ('ROLE_ADMIN');
INSERT INTO roles (role_name) VALUES ('ROLE_USER');

-- Insert into users
INSERT INTO users (
    created_at, account_non_expired, account_non_locked, balance, country_code,
    credentials_non_expired, email, enabled, is_email_verified, password, username, role_id
) VALUES (
             NOW(), true, true, 100.00, 'SG', true, 'admin@gmail.com', true, true, '1234', 'admin', 1
         ),
         (
             NOW(), true, true, 200.00, 'MM', true, 'user@gmail.com', true, true, '1234', 'user', 2
         );

-- Insert into package_plans
INSERT INTO package_plans (country_code, credits, expiry_date, name, price)
VALUES ('SG', 10, NOW() + INTERVAL '30 days', 'Basic Plan', 9.99),
       ('MY', 20, NOW() + INTERVAL '60 days', 'Premium Plan', 19.99);

-- Insert into user_packages
INSERT INTO user_packages (remaining_credits, package_plan_id, user_id)
VALUES (10, 1, 1),
       (15, 2, 2);

-- Insert into class_schedule
INSERT INTO class_schedule (country_code, description, end_time, max_capacity, name, required_credits, start_time)
VALUES ('SG', 'Yoga Class', NOW() + INTERVAL '2 hours', 20, 'Morning Yoga', 1, NOW()),
       ('MY', 'Pilates Class', NOW() + INTERVAL '3 hours', 15, 'Evening Pilates', 2, NOW());

-- Insert into bookings
INSERT INTO bookings (booked_at, class_id, package_id, user_id)
VALUES (NOW(), 1, 1, 1),
       (NOW(), 2, 2, 2);
