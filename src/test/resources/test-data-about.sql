-- Initial Seed Data
INSERT INTO about_section (description) VALUES ('Over the past 2.5+ years, my focus has been on engineering enterprise-grade solutions. At ISS-STOXX, I specialize in modernizing legacy architectures...');

INSERT INTO skill_categories (category_name) VALUES ('Frontend & UI'), ('Backend & APIs'), ('Data & Cloud');

INSERT INTO skills (category_id, skill_name, icon_name) VALUES
                                                            (1, 'JavaScript', 'FaJs'), (1, 'React', 'FaReact'), (1, 'Next.js', 'SiNextdotjs'),
                                                            (2, 'Java', 'FaJava'), (2, 'Spring Boot', 'SiSpringboot'), (2, 'Python', 'FaPython'),
                                                            (3, 'MySQL', 'SiMysql'), (3, 'MongoDB', 'SiMongodb'), (3, 'Azure', 'SiMicrosoftazure');


-- Seed Data for Experience (Extracted from your SS)
INSERT INTO experience (title, stage, description)
VALUES ('Full Stack Software Developer | ISS-STOXX', 'July 2023 - Present', 'Engineered the modernization of legacy Spring Boot and React architectures for enterprise climate applications. Optimized data ingestion pipelines for large-scale Parquet datasets, improving processing efficiency by 40%...');

-- Seed Data for Awards
INSERT INTO awards (title, stage, description)
VALUES ('Microsoft AI for Earth Grant ($15,000 USD)', '2021 - 2022', 'Awarded for an AI-based statistical analysis project on land-use plastic pollution.'),
       ('Deep Blue Hackathon Winner - 1st Place', '2023', 'Created a minutes of meeting generator inside MS Teams.');

INSERT INTO certifications (title, stage, description) VALUES
                                                           ('B.E. Computer Engineering', 'University of Mumbai', 'Secured a First Class with Distinction, graduating in 2023 with a CGPA of 9.7/10.0...'),
                                                           ('Machine Learning Specialization', 'Coursera (Andrew Ng)', 'Covered supervised learning, unsupervised learning, and best practices in machine learning.');
