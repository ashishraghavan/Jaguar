CREATE OR REPLACE FUNCTION make_plpgsql() RETURNS VOID LANGUAGE SQL AS $$ CREATE LANGUAGE plpgsql; $$;
SELECT
  CASE
  WHEN EXISTS(
      SELECT 1
      FROM pg_catalog.pg_language
      WHERE lanname='plpgsql'
  )
    THEN NULL
  ELSE make_plpgsql() END;

DROP FUNCTION make_plpgsql();
DROP TRIGGER IF EXISTS update_timestamp ON jaguar_account;
DROP TRIGGER IF EXISTS update_timestamp ON jaguar_user;
CREATE FUNCTION update_timestamp() RETURNS TRIGGER LANGUAGE plpgsql AS $$ BEGIN NEW.modified = CURRENT_TIMESTAMP; RETURN NEW; END; $$;
CREATE TRIGGER update_timestamp BEFORE INSERT OR UPDATE ON jaguar_account FOR EACH ROW EXECUTE PROCEDURE update_timestamp();
CREATE TRIGGER update_timestamp BEFORE INSERT OR UPDATE ON jaguar_address FOR EACH ROW EXECUTE PROCEDURE update_timestamp();
CREATE TRIGGER update_timestamp BEFORE INSERT OR UPDATE ON jaguar_application FOR EACH ROW EXECUTE PROCEDURE update_timestamp();
CREATE TRIGGER update_timestamp BEFORE INSERT OR UPDATE ON application_role FOR EACH ROW EXECUTE PROCEDURE update_timestamp();
CREATE TRIGGER update_timestamp BEFORE INSERT OR UPDATE ON jaguar_device FOR EACH ROW EXECUTE PROCEDURE update_timestamp();
CREATE TRIGGER update_timestamp BEFORE INSERT OR UPDATE ON device_application FOR EACH ROW EXECUTE PROCEDURE update_timestamp();
CREATE TRIGGER update_timestamp BEFORE INSERT OR UPDATE ON jaguar_role FOR EACH ROW EXECUTE PROCEDURE update_timestamp();
CREATE TRIGGER update_timestamp BEFORE INSERT OR UPDATE ON jaguar_user FOR EACH ROW EXECUTE PROCEDURE update_timestamp();
CREATE TRIGGER update_timestamp BEFORE INSERT OR UPDATE ON user_application FOR EACH ROW EXECUTE PROCEDURE update_timestamp();
CREATE TRIGGER update_timestamp BEFORE INSERT or UPDATE on user_role FOR EACH ROW EXECUTE PROCEDURE update_timestamp();
