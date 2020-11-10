CREATE TABLE session_limerick (
  id SERIAL PRIMARY KEY,
  limerick_id INTEGER NOT NULL REFERENCES limerick,
  session_id UUID NOT NULL
);
