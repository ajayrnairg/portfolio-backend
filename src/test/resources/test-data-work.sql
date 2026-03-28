-- 1. Insert Work Section Text (Extracted from your Screenshot)
INSERT INTO work_section (title, description)
VALUES (
   'Featured Engineering',
   'A curated selection of my technical builds, ranging from high-performance web applications to award-winning machine learning research. Each project highlights my focus on scalable architecture, cloud integration, and delivering seamless user experiences from the backend to the frontend.'
       );

-- 2. Insert Projects
INSERT INTO projects (title, thumbnail_path, description) VALUES
     ('High-Performance Developer Portfolio', '/thumb1.jpg', 'Engineered a custom, responsive portfolio application utilizing Server-Side Rendering (SSR) for optimal SEO and a 100/100 Lighthouse performance score. Implemented automated CI/CD deployment pipelines.'),
     ('Predictive Plastic Footprint Analysis (Microsoft AI Grant)', '/thumb2.jpg', 'Award-winning statistical analysis project mapping 10 years of land-use plastic pollution in India. Built predictive machine learning models hosted on Azure Virtual Machines.'),
     ('GistGator: AI Meeting Summarizer', '/thumb3.jpg', 'Developed an end-to-end NLP pipeline that ingests audio/video streams to generate automated meeting transcripts and abstractive summaries. Packaged as an interactive Chrome extension.');

-- 3. Insert Tech Stacks (Example for Project 1)
INSERT INTO project_tech_stack (project_id, tech_name) VALUES
       (1, 'SiNextdotjs'), (1, 'SiReact'), (1, 'FaJava'), (1, 'SiSpringboot'), (1, 'SiPostgresql'),
       (2, 'SiMicrosoftazure'), (2, 'SiPython'), (2, 'SiPandas');

-- 4. Insert CTA Links (Example for Project 1)
INSERT INTO project_links (project_id, label, icon_name, url) VALUES
  (1, 'View Code', 'FaGithub', 'https://github.com'),
  (1, 'Live Demo', 'VscLiveShare', 'https://example.com'),
  (1, 'Video Demo', 'FaVideo', 'https://example.com'),
  (1, 'Research Paper', 'IoNewspaperOutline', 'https://example.com');
