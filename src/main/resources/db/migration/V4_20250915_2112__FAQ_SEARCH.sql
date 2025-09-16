ALTER TABLE faq
    ADD COLUMN search_vector tsvector;

CREATE INDEX idx_faq_search_vector ON faq USING GIN (search_vector);

CREATE OR REPLACE FUNCTION faq_search_vector_trigger() RETURNS trigger AS
$$
BEGIN
    NEW.search_vector := to_tsvector(
            'russian',
            coalesce(NEW.question, '') || ' ' || coalesce(NEW.keywords::text, '')
                         );
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

CREATE TRIGGER tsvectorupdate
    BEFORE INSERT OR UPDATE
    ON faq
    FOR EACH ROW
EXECUTE FUNCTION faq_search_vector_trigger();