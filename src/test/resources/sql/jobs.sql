CREATE TABLE jobs(
   id UUID DEFAULT GEN_RANDOM_UUID(),
   date BIGINT NOT NULL,
   ownerEmail TEXT NOT NULL,
   title TEXT NOT NULL,
   company TEXT NOT NULL,
   description TEXT NOT NULL,
   externalUrl TEXT NOT NULL,
   location TEXT NOT NULL,
   remote BOOLEAN NOT NULL DEFAULT FALSE,
   seniority TEXT,
   salaryLow INTEGER,
   salaryHigh INTEGER,
   currency TEXT,
   country TEXT,
   tags TEXT[],
   image TEXT,
   other TEXT,
   active BOOLEAN NOT NULL DEFAULT FALSE
);

ALTER TABLE jobs
ADD CONSTRAINT PK_JOBS PRIMARY KEY (id);

INSERT INTO jobs (
   id,
   date,
   ownerEmail,
   title,
   company,
   description,
   externalUrl,
   location,
   remote,
   seniority,
   salaryLow,
   salaryHigh,
   currency,
   country,
   tags,
   image,
   other,
   active
) VALUES (
   '843df718-ec6e-4d49-9289-f799c0f40064',
   1659186086,
   'email@example.com',
   'Example Company',
   'Senior Scala',
   'A long description of Scala job',
   'https://example.com/applications',
   'Sudan',
   false,
   'Senior',
   2000,
   4000,
   'USD',
   'KRT',
   ARRAY['scala', 'scala-3', 'cats'],
   NULL,
   NULL,
   false
)