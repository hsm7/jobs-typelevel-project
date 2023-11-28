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