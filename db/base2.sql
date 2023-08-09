CREATE DATABASE IF NOT EXISTS webmobile;
USE webmobile;

CREATE TABLE foto_utenti (
   path_foto VARCHAR(400) NOT NULL,
   PRIMARY KEY (path_foto)
);

CREATE TABLE utente (
   username VARCHAR(50) NOT NULL,
   nome VARCHAR(50) NOT NULL,
   cognome VARCHAR(50) NOT NULL,
   email VARCHAR(80) NOT NULL UNIQUE,
   pass VARCHAR(64) NOT NULL,
   domanda VARCHAR(400) NOT NULL,
   risposta VARCHAR(400) NOT NULL,
   path_foto VARCHAR(400) NOT NULL DEFAULT "media/images/foto_utenti/default.png",
   cod_fiscale VARCHAR(16) NOT NULL,
   punti_correnti INT UNSIGNED NOT NULL DEFAULT 10000,
   num_carta VARCHAR(16) NOT NULL UNIQUE,
   scadenza_carta DATE NOT NULL,
   cod_sicurezza_carta VARCHAR(3) NOT NULL,
   importo_carta DOUBLE NOT NULL DEFAULT 100000.0,
   PRIMARY KEY (username),
   FOREIGN KEY (path_foto) REFERENCES foto_utenti(path_foto)
);

ALTER TABLE utente
   ADD CONSTRAINT nome CHECK (nome REGEXP '^[A-Z\' ]+$'), -- Qua non serve A-Za-z ' ma da Kotlin si: They are case insensitive, unless you do a binary comparison (vedi IBAN Ing.Software).
   ADD CONSTRAINT cognome CHECK (cognome REGEXP '^[A-Z\' ]+$'),
   ADD CONSTRAINT pass CHECK (pass <> "7b756fb9b42446a8fba99b94d9270f7faf1797ec955a4f49066081424476891f"),
   ADD CONSTRAINT codice_fiscale CHECK (cod_fiscale REGEXP '^[A-Z]{6}[0-9]{2}[A-Z]{1}[0-9]{2}[A-Z]{1}[0-9]{3}[A-Z]{1}$'),
   ADD CONSTRAINT numero_carta_di_credito CHECK (num_carta REGEXP '^[0-9]{16}$'),
   ADD CONSTRAINT codice_sicurezza_carta CHECK (cod_sicurezza_carta REGEXP '^[0-9]{3}$'),
   ADD CONSTRAINT importo_carta CHECK (importo_carta >= 0.0);

DELIMITER //
CREATE TRIGGER controllaScadenzaCartaInsert
BEFORE INSERT
ON utente
FOR EACH ROW
BEGIN
   IF NEW.scadenza_carta < CURDATE() THEN
      SIGNAL SQLSTATE '50001' SET MESSAGE_TEXT = 'ScadenzaCartaError';
   END IF;
END; //
DELIMITER ;

DELIMITER //
CREATE TRIGGER controllaScadenzaCartaUpdate
BEFORE UPDATE
ON utente
FOR EACH ROW
BEGIN
   IF NEW.scadenza_carta < CURDATE() THEN
      SIGNAL SQLSTATE '50001' SET MESSAGE_TEXT = 'ScadenzaCartaError';
   END IF;
END; //
DELIMITER ;

CREATE TABLE segnalazione (
   cod_segnalazione INT UNSIGNED NOT NULL AUTO_INCREMENT,
   ref_utente VARCHAR(50) NOT NULL,
   testo VARCHAR(400) NOT NULL,
   PRIMARY KEY (cod_segnalazione, ref_utente),
   FOREIGN KEY (ref_utente) REFERENCES utente(username)
);

CREATE TABLE albergo (
   indirizzo VARCHAR(200) NOT NULL,
   coordinate VARCHAR(50) NOT NULL,
   nome VARCHAR(50) NOT NULL,
   telefono VARCHAR(13) NOT NULL UNIQUE,
   email VARCHAR(80) NOT NULL UNIQUE,
   sito VARCHAR(80) NOT NULL UNIQUE,
   inizio_check TIME NOT NULL,
   fine_check TIME NOT NULL,
   PRIMARY KEY (indirizzo)
);

ALTER TABLE albergo
   ADD CONSTRAINT telefono CHECK (telefono REGEXP '^\\+[0-9]{12}$');

CREATE TABLE foto_albergo (
   path_foto VARCHAR(400) NOT NULL,
   descrizione_foto VARCHAR(400) NOT NULL,
   ref_albergo VARCHAR(200) NOT NULL,
   PRIMARY KEY (path_foto),
   FOREIGN KEY (ref_albergo) REFERENCES albergo(indirizzo)
);

CREATE TABLE camera (
   num_camera INT UNSIGNED NOT NULL,
   ref_albergo VARCHAR(200) NOT NULL,
   nome_camera VARCHAR(50) NOT NULL,
   descrizione VARCHAR(400) NOT NULL,
   num_letti TINYINT UNSIGNED NOT NULL,
   accesso_disabili TINYINT UNSIGNED NOT NULL,
   accesso_animali TINYINT UNSIGNED NOT NULL,
   in_offerta TINYINT UNSIGNED NOT NULL,
   balcone TINYINT UNSIGNED NOT NULL,
   bagno TINYINT UNSIGNED NOT NULL,
   tv TINYINT UNSIGNED NOT NULL,
   wifi TINYINT UNSIGNED NOT NULL,
   prezzo DOUBLE NOT NULL,
   piscina TINYINT UNSIGNED NOT NULL,
   PRIMARY KEY (num_camera, ref_albergo),
   FOREIGN KEY (ref_albergo) REFERENCES albergo(indirizzo)
);

ALTER TABLE camera
   ADD CONSTRAINT num_letti CHECK (num_letti BETWEEN 1 AND 5),
   ADD CONSTRAINT accesso_disabili CHECK (accesso_disabili BETWEEN 0 AND 1),
   ADD CONSTRAINT accesso_animali CHECK (accesso_animali BETWEEN 0 AND 1),
   ADD CONSTRAINT in_offerta CHECK (in_offerta BETWEEN 0 AND 1),
   ADD CONSTRAINT balcone CHECK (balcone BETWEEN 0 AND 1),
   ADD CONSTRAINT bagno CHECK (bagno BETWEEN 0 AND 1),
   ADD CONSTRAINT tv CHECK (tv BETWEEN 0 AND 1),
   ADD CONSTRAINT wifi CHECK (wifi BETWEEN 0 AND 1),
   ADD CONSTRAINT prezzo CHECK (prezzo > 0.0),
   ADD CONSTRAINT piscina CHECK (piscina BETWEEN 0 AND 1);

CREATE TABLE foto_camera (
   path_foto VARCHAR(400) NOT NULL,
   ref_camera INT UNSIGNED NOT NULL,
   ref_albergo VARCHAR(200) NOT NULL,
   predefinita TINYINT UNSIGNED NOT NULL DEFAULT 0,
   PRIMARY KEY (path_foto),
   FOREIGN KEY (ref_camera, ref_albergo) REFERENCES camera(num_camera, ref_albergo)
);

ALTER TABLE foto_camera
   ADD CONSTRAINT predefinita CHECK (predefinita BETWEEN 0 AND 1);

DELIMITER //
CREATE TRIGGER verificaFotoPredefinitaInsert
BEFORE INSERT
ON foto_camera
FOR EACH ROW
BEGIN
   IF NEW.predefinita = 1 THEN
      IF EXISTS (SELECT * FROM foto_camera WHERE ref_camera = NEW.ref_camera AND ref_albergo = NEW.ref_albergo AND predefinita = 1) THEN
         SIGNAL SQLSTATE '50001' SET MESSAGE_TEXT = 'FotoCameraPrefefinitaError';
      END IF;
   END IF;
END; //
DELIMITER ;

DELIMITER //
CREATE TRIGGER verificaFotoPredefinitaUpdate
BEFORE UPDATE
ON foto_camera
FOR EACH ROW
BEGIN
   IF NEW.predefinita = 1 THEN
      IF EXISTS (SELECT * FROM foto_camera WHERE ref_camera = NEW.ref_camera AND ref_albergo = NEW.ref_albergo AND predefinita = 1) THEN
         SIGNAL SQLSTATE '50001' SET MESSAGE_TEXT = 'FotoCameraPrefefinitaError';
      END IF;
   END IF;
END; //
DELIMITER ;

CREATE TABLE prenotazione (
   ref_utente VARCHAR(50) NOT NULL,
   ref_camera INT UNSIGNED NOT NULL,
   ref_albergo VARCHAR(200) NOT NULL,
   data_inizio DATE NOT NULL,
   data_fine DATE NOT NULL,
   check_in_effettivo TIME DEFAULT NULL,
   check_out_effettivo TIME DEFAULT NULL,
   testo_recensione VARCHAR(400) DEFAULT NULL,
   punteggio_recensione DOUBLE DEFAULT NULL,
   PRIMARY KEY (ref_utente, ref_camera, ref_albergo, data_inizio),
   FOREIGN KEY (ref_utente) REFERENCES utente(username),
   FOREIGN KEY (ref_camera, ref_albergo) REFERENCES camera(num_camera, ref_albergo)
);

ALTER TABLE prenotazione
   ADD CONSTRAINT punteggio_recensione CHECK (punteggio_recensione BETWEEN 1.0 AND 5.0);

CREATE TABLE promozione (
   cod_promozione INT UNSIGNED NOT NULL AUTO_INCREMENT,
   importo DOUBLE NOT NULL,
   punti_richiesti INT UNSIGNED NOT NULL,
   tipo TINYINT UNSIGNED NOT NULL, -- 0 = ordinaria, 1 = speciale
   usata TINYINT UNSIGNED NOT NULL DEFAULT 0,
   ref_utente VARCHAR(50),
   PRIMARY KEY (cod_promozione),
   FOREIGN KEY (ref_utente) REFERENCES utente(username)
);

ALTER TABLE promozione
   ADD CONSTRAINT tipo CHECK (tipo BETWEEN 0 AND 1),
   ADD CONSTRAINT usata CHECK (usata BETWEEN 0 AND 1),
   ADD CONSTRAINT importo CHECK (importo > 0.0),
   AUTO_INCREMENT = 1234;

DELIMITER //
CREATE TRIGGER controllaPromozione
BEFORE UPDATE
ON promozione
FOR EACH ROW
BEGIN
   IF OLD.ref_utente IS NOT NULL AND OLD.usata = 1 THEN
      SIGNAL SQLSTATE '50001' SET MESSAGE_TEXT = 'ScadenzaPromozioneError';
   END IF;
END; //
DELIMITER ;

INSERT INTO foto_utenti(path_foto) VALUES ("media/images/foto_utenti/default.png");
INSERT INTO foto_utenti(path_foto) VALUES ("media/images/foto_utenti/foto1.png");
INSERT INTO foto_utenti(path_foto) VALUES ("media/images/foto_utenti/foto2.png");
INSERT INTO foto_utenti(path_foto) VALUES ("media/images/foto_utenti/foto3.png");
INSERT INTO foto_utenti(path_foto) VALUES ("media/images/foto_utenti/foto4.png");
INSERT INTO foto_utenti(path_foto) VALUES ("media/images/foto_utenti/foto5.png");

INSERT INTO utente(username, nome, cognome, email, pass, domanda, risposta, cod_fiscale, num_carta, scadenza_carta, cod_sicurezza_carta) VALUES ("mazza", "Nicolo'", "La Rosa Mazza", "nicololarosamazza2@mailfalsa.com", "8e35c2cd3bf6641bdb0e2050b76932cbb2e6034a0ddacc1d9bea82a6ba57f7cf", "Qual e' il nome del tuo cane?", "Leo", "ABCDEF02D02G273W", "1234567812345678", "2024-06-14", "123"); -- Password: q
INSERT INTO utente(username, nome, cognome, email, pass, domanda, risposta, cod_fiscale, num_carta, scadenza_carta, cod_sicurezza_carta) VALUES ("gabriele", "Gabriele", "Bova", "gabriele.bova01@mailfalsa.com", "7dcf407fa84a0e0519c7991154c4148de0244d7589020c0d9842db9efad82094", "Quanto fa 15+18?", "36", "ABCDEF01D18G273W", "1234567912345679", "2025-06-14", "456"); -- Password: a1b2c3d4
INSERT INTO utente(username, nome, cognome, email, pass, domanda, risposta, cod_fiscale, num_carta, scadenza_carta, cod_sicurezza_carta) VALUES ("prova", "Mario", "Rossi", "mario.rossi@mailfalsa.com", "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08", "Qual è il numero dell'aula delle lezioni?", "180", "ABCDEF02A23G273Y", "1234567912345677", "2026-06-14", "789"); -- Password: test

-- Injection vecchia: Username (esistente) = mazza';" -- | Password [qualsiasi]
-- Injection nuova: Username (esistente) = mazza' USING BINARY); -- (meno meno spazio) | Password [qualsiasi]

INSERT INTO segnalazione(ref_utente, testo) VALUES ("mazza", "La camera era sporca al momento del mio arrivo. Il pavimento non sembrava essere stato pulito e c'erano macchie evidenti su alcuni mobili.");
INSERT INTO segnalazione(ref_utente, testo) VALUES ("gabriele", "Il servizio in camera e' stato molto lento. Ho dovuto aspettare piu' di un'ora per ricevere il mio ordine, e quando e' arrivato, il cibo era freddo.");
INSERT INTO segnalazione(ref_utente, testo) VALUES ("mazza", "Il condizionatore d'aria nella mia camera non funzionava correttamente. Non riusciva a raffreddare l'ambiente e faceva un rumore molto forte, disturbando il mio riposo.");
INSERT INTO segnalazione(ref_utente, testo) VALUES ("prova", "La connessione Wi-Fi nell'hotel era molto instabile. Ho avuto difficolta' a connettermi e la connessione si interrompeva frequentemente, rendendo impossibile lavorare o guardare video online.");
INSERT INTO segnalazione(ref_utente, testo) VALUES ("gabriele", "La colazione inclusa era di bassa qualita'. Gli alimenti erano scadenti e poco appetitosi, e la varieta' di scelta era molto limitata. Non rispecchiava le aspettative di un albergo di questa categoria.");

INSERT INTO albergo(indirizzo, coordinate, nome, telefono, email, sito, inizio_check, fine_check) VALUES ("Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "38.1029897,13.3462739", "HotelCode", "+391234567890", "hotelcode@mailfalsa.com", "https://www.hotelcode.it", "00:00:00", "12:00:00");

INSERT INTO foto_albergo(path_foto, descrizione_foto, ref_albergo) VALUES ("media/images/hotelcode/foto_albergo/piscina.png", "La nostra struttura vanta una splendida piscina, un'oasi di serenità dove potrete rinfrescarvi e godervi momenti di puro piacere. Concedetevi un tuffo rigenerante e lasciatevi cullare dalla dolce melodia del relax.", "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_albergo(path_foto, descrizione_foto, ref_albergo) VALUES ("media/images/hotelcode/foto_albergo/ingresso.png", "Da oltre 50 anni, il nostro hotel nel cuore della citta' offre un'esperienza di ospitalita' senza pari. Garantiamo servizio impeccabile, comfort di prima classe e un'atmosfera accogliente. La nostra esperienza pluridecennale si traduce in un soggiorno indimenticabile per i nostri ospiti.", "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_albergo(path_foto, descrizione_foto, ref_albergo) VALUES ("media/images/hotelcode/foto_albergo/palestra.png", "Presso la nostra struttura troverete una moderna palestra, dotata di attrezzature all'avanguardia, per permettervi di mantenervi in forma durante il vostro soggiorno. Dedicate del tempo al vostro allenamento e lasciate che la nostra palestra diventi il vostro rifugio.", "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_albergo(path_foto, descrizione_foto, ref_albergo) VALUES ("media/images/hotelcode/foto_albergo/reception.png", "Vuoi contattarci? I nostri contatti:", "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_albergo(path_foto, descrizione_foto, ref_albergo) VALUES ("media/images/hotelcode/foto_albergo/mappa_edificio.png", "Qui di fianco trovate la mappa del piano nel quale è situata la nostra reception:", "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");

INSERT INTO camera(num_camera, ref_albergo, nome_camera, descrizione, num_letti, accesso_disabili, accesso_animali, in_offerta, balcone, bagno, tv, wifi, prezzo, piscina) VALUES (101, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "Suite del Sole", "La camera è semplicemente meravigliosa. Spaziosa, pulita e arredata con gusto. Il letto è incredibilmente comodo e la vista panoramica è mozzafiato. Un'oasi di comfort e relax consigliata vivamente per un soggiorno indimenticabile!", 5, 0, 1, 1, 1, 1, 0, 0, 50.0, 1);
INSERT INTO camera(num_camera, ref_albergo, nome_camera, descrizione, num_letti, accesso_disabili, accesso_animali, in_offerta, balcone, bagno, tv, wifi, prezzo, piscina) VALUES (102, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "Blu Mare", "La camera offre un'esperienza di soggiorno elegante e confortevole. Arredata con gusto, questa spaziosa camera è dotata di comfort moderni e servizi di alta qualità.", 1, 1, 0, 0, 1, 1, 1, 1, 60.0, 1);
INSERT INTO camera(num_camera, ref_albergo, nome_camera, descrizione, num_letti, accesso_disabili, accesso_animali, in_offerta, balcone, bagno, tv, wifi, prezzo, piscina) VALUES (103, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "Vista Monti", "La camera deluxe è la soluzione perfetta per chi cerca il massimo comfort e lusso. Con una metratura generosa, arredi di pregio e servizi esclusivi, questa camera ti farà sentire un vero ospite di riguardo.", 2, 1, 1, 1, 1, 1, 0, 1, 70.0, 1);
INSERT INTO camera(num_camera, ref_albergo, nome_camera, descrizione, num_letti, accesso_disabili, accesso_animali, in_offerta, balcone, bagno, tv, wifi, prezzo, piscina) VALUES (104, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "Suite Romantica", "La camera è l'ideale per un soggiorno romantico. L'atmosfera accogliente, i dettagli curati e la vista mozzafiato creano il perfetto scenario per trascorrere momenti indimenticabili.", 4, 1, 1, 1, 1, 1, 0, 1, 80.0, 1);
INSERT INTO camera(num_camera, ref_albergo, nome_camera, descrizione, num_letti, accesso_disabili, accesso_animali, in_offerta, balcone, bagno, tv, wifi, prezzo, piscina) VALUES (105, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "Deluxe City View", "La nostra camera Deluxe City View è un'oasi di lusso e comfort. Con i suoi arredi eleganti e una vista mozzafiato sulla città, offre un'esperienza indimenticabile. Goditi il relax nel comfort del tuo ambiente, dotato di tutte le comodità.", 3, 0, 0, 1, 0, 0, 0, 1, 90.0, 0);
INSERT INTO camera(num_camera, ref_albergo, nome_camera, descrizione, num_letti, accesso_disabili, accesso_animali, in_offerta, balcone, bagno, tv, wifi, prezzo, piscina) VALUES (106, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "Suite Elegante", "La camera executive offre comfort e stile per i viaggiatori d'affari. Con uno spazio di lavoro dedicato e servizi esclusivi, questa camera ti permette di lavorare in tranquillità e di rilassarti dopo una lunga giornata di impegni.", 2, 1, 1, 0, 0, 1, 1, 1, 100.0, 1);
INSERT INTO camera(num_camera, ref_albergo, nome_camera, descrizione, num_letti, accesso_disabili, accesso_animali, in_offerta, balcone, bagno, tv, wifi, prezzo, piscina) VALUES (107, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "Familiare Garden", "La camera familiare offre spazio e comfort per tutta la famiglia. Con letti extra e servizi dedicati ai più piccoli, questa camera è la soluzione perfetta per una vacanza in famiglia indimenticabile.", 5, 0, 1, 1, 0, 0, 0, 0, 50.0, 1);
INSERT INTO camera(num_camera, ref_albergo, nome_camera, descrizione, num_letti, accesso_disabili, accesso_animali, in_offerta, balcone, bagno, tv, wifi, prezzo, piscina) VALUES (108, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "Loft Moderno", "La camera suite ti offre uno spazio ampio e lussuoso per il tuo soggiorno. Gli arredi eleganti, le comodità esclusive e i servizi di alta qualità garantiscono un'esperienza indimenticabile.", 4, 1, 1, 0, 1, 0, 0, 0, 60.0, 1);
INSERT INTO camera(num_camera, ref_albergo, nome_camera, descrizione, num_letti, accesso_disabili, accesso_animali, in_offerta, balcone, bagno, tv, wifi, prezzo, piscina) VALUES (109, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "Executive Panorama", "La camera con terrazza panoramica offre una vista mozzafiato sulla città circostante. Goditi la tua colazione o un drink serale sulla terrazza e ammira i panorami spettacolari.", 4, 1, 0, 1, 0, 0, 0, 1, 70.0, 0);
INSERT INTO camera(num_camera, ref_albergo, nome_camera, descrizione, num_letti, accesso_disabili, accesso_animali, in_offerta, balcone, bagno, tv, wifi, prezzo, piscina) VALUES (110, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "Suite Presidenziale", "La camera doppia offre comfort e funzionalità per un soggiorno piacevole. Con i suoi servizi essenziali e un design accogliente, questa camera è ideale per i viaggiatori che cercano una sistemazione pratica.", 2, 0, 0, 1, 1, 0, 1, 1, 80.0, 0);
INSERT INTO camera(num_camera, ref_albergo, nome_camera, descrizione, num_letti, accesso_disabili, accesso_animali, in_offerta, balcone, bagno, tv, wifi, prezzo, piscina) VALUES (111, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "Camera Relax & Spa", "La camera quadrupla offre spazio e comfort per gruppi o famiglie numerose. Con letti extra e servizi dedicati, questa camera assicura un soggiorno piacevole per tutti i suoi ospiti.", 2, 0, 1, 0, 0, 0, 0, 0, 90.0, 1);
INSERT INTO camera(num_camera, ref_albergo, nome_camera, descrizione, num_letti, accesso_disabili, accesso_animali, in_offerta, balcone, bagno, tv, wifi, prezzo, piscina) VALUES (112, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "Deluxe Suite Terrazza", "La camera con letti singoli è la scelta ideale per i viaggiatori che preferiscono letti separati. Con il suo design funzionale e comfort essenziali, questa camera ti offre un piacevole riposo durante il tuo soggiorno.", 1, 1, 1, 0, 1, 1, 1, 1, 100.0, 0);
INSERT INTO camera(num_camera, ref_albergo, nome_camera, descrizione, num_letti, accesso_disabili, accesso_animali, in_offerta, balcone, bagno, tv, wifi, prezzo, piscina) VALUES (113, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "Vintage Chic", "La camera con angolo cottura è perfetta per chi desidera una maggiore indipendenza durante il soggiorno. Con una piccola cucina attrezzata, potrai preparare i tuoi pasti e goderti il comfort di casa tua.", 3, 1, 1, 1, 1, 1, 1, 0, 50.0, 1);
INSERT INTO camera(num_camera, ref_albergo, nome_camera, descrizione, num_letti, accesso_disabili, accesso_animali, in_offerta, balcone, bagno, tv, wifi, prezzo, piscina) VALUES (114, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "Suite Lusso Esclusiva", "La camera con camino è perfetta per le serate romantiche. Accendi il camino, rilassati sul comodo divano e crea un'atmosfera calda e accogliente per il tuo soggiorno.", 4, 0, 1, 1, 0, 1, 1, 0, 60.0, 1);
INSERT INTO camera(num_camera, ref_albergo, nome_camera, descrizione, num_letti, accesso_disabili, accesso_animali, in_offerta, balcone, bagno, tv, wifi, prezzo, piscina) VALUES (115, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "Zen Garden", "Immergiti nella tranquillità della nostra Suite Zen Garden. Questa camera è un vero e proprio paradiso di serenità, con un design minimalista e una vista incantevole sul nostro rigoglioso giardino zen. Lasciati avvolgere dalla pace.", 1, 0, 1, 0, 1, 1, 0, 1, 70.0, 1);
INSERT INTO camera(num_camera, ref_albergo, nome_camera, descrizione, num_letti, accesso_disabili, accesso_animali, in_offerta, balcone, bagno, tv, wifi, prezzo, piscina) VALUES (116, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "Loft Urbano", "La camera standard offre un soggiorno confortevole e senza fronzoli. Con i suoi servizi essenziali e il design accogliente, questa camera ti permette di goderti il tuo soggiorno a prezzi convenienti.", 4, 0, 0, 1, 1, 1, 0, 0, 80.0, 1);
INSERT INTO camera(num_camera, ref_albergo, nome_camera, descrizione, num_letti, accesso_disabili, accesso_animali, in_offerta, balcone, bagno, tv, wifi, prezzo, piscina) VALUES (117, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "Design Contemporaneo", "La camera con vasca idromassaggio è la scelta perfetta per il relax totale. Immergiti nella vasca idromassaggio, lascia che lo stress svanisca e concediti un soggiorno di puro benessere.", 5, 0, 1, 1, 0, 0, 0, 1, 90.0, 0);
INSERT INTO camera(num_camera, ref_albergo, nome_camera, descrizione, num_letti, accesso_disabili, accesso_animali, in_offerta, balcone, bagno, tv, wifi, prezzo, piscina) VALUES (118, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "Suite Jacuzzi Privata", "La camera romantica è ideale per una fuga d'amore. L'atmosfera intima, i dettagli romantici e il comfort esclusivo creano il perfetto nido d'amore per trascorrere momenti speciali.", 5, 1, 1, 1, 0, 1, 0, 0, 100.0, 1);
INSERT INTO camera(num_camera, ref_albergo, nome_camera, descrizione, num_letti, accesso_disabili, accesso_animali, in_offerta, balcone, bagno, tv, wifi, prezzo, piscina) VALUES (119, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "Panoramica Fiume", "La camera con letto a baldacchino ti immergerà in un'atmosfera regale. Con il suo letto elegante e il design raffinato, questa camera ti farà sentire come un vero principe o principessa.", 2, 0, 1, 0, 1, 0, 1, 1, 50.0, 1);
INSERT INTO camera(num_camera, ref_albergo, nome_camera, descrizione, num_letti, accesso_disabili, accesso_animali, in_offerta, balcone, bagno, tv, wifi, prezzo, piscina) VALUES (120, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "Tematica Art Deco", "La camera con area relax ti offre uno spazio dedicato al benessere. Con una zona relax con comode poltrone e servizi dedicati, potrai goderti momenti di puro relax durante il tuo soggiorno.", 4, 0, 0, 0, 1, 0, 0, 1, 60.0, 1);
INSERT INTO camera(num_camera, ref_albergo, nome_camera, descrizione, num_letti, accesso_disabili, accesso_animali, in_offerta, balcone, bagno, tv, wifi, prezzo, piscina) VALUES (121, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "Suite Relax Vista Mare", "La camera con vista mare regala panorami mozzafiato direttamente dalla tua finestra. Risvegliati con la brezza marina e ammira la bellezza del mare ogni giorno del tuo soggiorno.", 4, 1, 0, 0, 0, 1, 0, 0, 70.0, 1);
INSERT INTO camera(num_camera, ref_albergo, nome_camera, descrizione, num_letti, accesso_disabili, accesso_animali, in_offerta, balcone, bagno, tv, wifi, prezzo, piscina) VALUES (122, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "Deluxe Balcone", "La camera con vista giardino offre una vista rilassante sul verde circostante. Svegliati con il canto degli uccelli e goditi la tranquillità della natura direttamente dalla tua finestra.", 4, 0, 1, 0, 0, 0, 0, 0, 80.0, 0);
INSERT INTO camera(num_camera, ref_albergo, nome_camera, descrizione, num_letti, accesso_disabili, accesso_animali, in_offerta, balcone, bagno, tv, wifi, prezzo, piscina) VALUES (123, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "Junior Suite Elegante", "La camera con sauna privata è l'ideale per il relax e il benessere. Con la tua sauna privata direttamente in camera, potrai goderti momenti di totale distensione e rigenerazione.", 1, 1, 1, 0, 1, 1, 0, 1, 90.0, 0);
INSERT INTO camera(num_camera, ref_albergo, nome_camera, descrizione, num_letti, accesso_disabili, accesso_animali, in_offerta, balcone, bagno, tv, wifi, prezzo, piscina) VALUES (124, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "Executive Skyline", "La camera suite è la soluzione perfetta per chi cerca l'ultima parola in termini di lusso e comfort. Con una metratura generosa, una zona living separata e servizi esclusivi, questa suite ti farà sentire come un vero VIP.", 3, 1, 0, 0, 0, 0, 1, 0, 100.0, 0);
INSERT INTO camera(num_camera, ref_albergo, nome_camera, descrizione, num_letti, accesso_disabili, accesso_animali, in_offerta, balcone, bagno, tv, wifi, prezzo, piscina) VALUES (125, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "Suite Loft Design", "La camera deluxe ti offre un soggiorno di lusso e comfort. Gli spazi ampi, l'arredamento elegante e i servizi di alta qualità rendono questa camera una scelta ideale per gli ospiti più esigenti.", 4, 0, 0, 0, 1, 0, 0, 0, 50.0, 0);

INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo, predefinita) VALUES ("media/images/hotelcode/camera1/foto1.png", 101, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", 1);
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera1/foto2.png", 101, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera1/foto3.png", 101, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera1/foto4.png", 101, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera1/foto5.png", 101, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera1/foto6.png", 101, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo, predefinita) VALUES ("media/images/hotelcode/camera2/foto1.png", 102, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", 1);
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera2/foto2.png", 102, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera2/foto3.png", 102, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera2/foto4.png", 102, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera2/foto5.png", 102, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera2/foto6.png", 102, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo, predefinita) VALUES ("media/images/hotelcode/camera3/foto1.png", 103, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", 1);
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera3/foto2.png", 103, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera3/foto3.png", 103, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera3/foto4.png", 103, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera3/foto5.png", 103, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera3/foto6.png", 103, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo, predefinita) VALUES ("media/images/hotelcode/camera4/foto1.png", 104, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", 1);
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera4/foto2.png", 104, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera4/foto3.png", 104, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera4/foto4.png", 104, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera4/foto5.png", 104, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera4/foto6.png", 104, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera4/foto7.png", 104, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera4/foto8.png", 104, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo, predefinita) VALUES ("media/images/hotelcode/camera5/foto1.png", 105, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", 1);
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera5/foto2.png", 105, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera5/foto3.png", 105, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera5/foto4.png", 105, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera5/foto5.png", 105, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo, predefinita) VALUES ("media/images/hotelcode/camera6/foto1.png", 106, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", 1);
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera6/foto2.png", 106, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera6/foto3.png", 106, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera6/foto4.png", 106, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera6/foto5.png", 106, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera6/foto6.png", 106, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo, predefinita) VALUES ("media/images/hotelcode/camera7/foto1.png", 107, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", 1);
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera7/foto2.png", 107, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera7/foto3.png", 107, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera7/foto4.png", 107, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera7/foto5.png", 107, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera7/foto6.png", 107, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo, predefinita) VALUES ("media/images/hotelcode/camera8/foto1.png", 108, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", 1);
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera8/foto2.png", 108, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera8/foto3.png", 108, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera8/foto4.png", 108, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera8/foto5.png", 108, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera8/foto6.png", 108, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo, predefinita) VALUES ("media/images/hotelcode/camera9/foto1.png", 109, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", 1);
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera9/foto2.png", 109, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera9/foto3.png", 109, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera9/foto4.png", 109, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo, predefinita) VALUES ("media/images/hotelcode/camera10/foto1.png", 110, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", 1);
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera10/foto2.png", 110, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera10/foto3.png", 110, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera10/foto4.png", 110, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera10/foto5.png", 110, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo, predefinita) VALUES ("media/images/hotelcode/camera11/foto1.png", 111, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", 1);
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera11/foto2.png", 111, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera11/foto3.png", 111, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera11/foto4.png", 111, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera11/foto5.png", 111, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo, predefinita) VALUES ("media/images/hotelcode/camera12/foto1.png", 112, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", 1);
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera12/foto2.png", 112, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo, predefinita) VALUES ("media/images/hotelcode/camera13/foto1.png", 113, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", 1);
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera13/foto2.png", 113, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera13/foto3.png", 113, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo, predefinita) VALUES ("media/images/hotelcode/camera14/foto1.png", 114, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", 1);
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera14/foto2.png", 114, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera14/foto3.png", 114, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera14/foto4.png", 114, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera14/foto5.png", 114, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo, predefinita) VALUES ("media/images/hotelcode/camera15/foto1.png", 115, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", 1);
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera15/foto2.png", 115, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera15/foto3.png", 115, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo, predefinita) VALUES ("media/images/hotelcode/camera16/foto1.png", 116, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", 1);
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera16/foto2.png", 116, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera16/foto3.png", 116, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera16/foto4.png", 116, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo, predefinita) VALUES ("media/images/hotelcode/camera17/foto1.png", 117, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", 1);
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera17/foto2.png", 117, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera17/foto3.png", 117, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera17/foto4.png", 117, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo, predefinita) VALUES ("media/images/hotelcode/camera18/foto1.png", 118, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", 1);
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera18/foto2.png", 118, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera18/foto3.png", 118, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera18/foto4.png", 118, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera18/foto5.png", 118, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo, predefinita) VALUES ("media/images/hotelcode/camera19/foto1.png", 119, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", 1);
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera19/foto2.png", 119, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera19/foto3.png", 119, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera19/foto4.png", 119, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera19/foto5.png", 119, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera19/foto6.png", 119, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera19/foto7.png", 119, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo, predefinita) VALUES ("media/images/hotelcode/camera20/foto1.png", 120, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", 1);
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera20/foto2.png", 120, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera20/foto3.png", 120, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera20/foto4.png", 120, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera20/foto5.png", 120, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo, predefinita) VALUES ("media/images/hotelcode/camera21/foto1.png", 121, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", 1);
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera21/foto2.png", 121, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera21/foto3.png", 121, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera21/foto4.png", 121, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera21/foto5.png", 121, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera21/foto6.png", 121, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera21/foto7.png", 121, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo, predefinita) VALUES ("media/images/hotelcode/camera22/foto1.png", 122, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", 1);
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera22/foto2.png", 122, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera22/foto3.png", 122, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo, predefinita) VALUES ("media/images/hotelcode/camera23/foto1.png", 123, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", 1);
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera23/foto2.png", 123, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera23/foto3.png", 123, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera23/foto4.png", 123, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo, predefinita) VALUES ("media/images/hotelcode/camera24/foto1.png", 124, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", 1);
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera24/foto2.png", 124, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera24/foto3.png", 124, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera24/foto4.png", 124, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera24/foto5.png", 124, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo, predefinita) VALUES ("media/images/hotelcode/camera25/foto1.png", 125, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", 1);
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera25/foto2.png", 125, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera25/foto3.png", 125, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera25/foto4.png", 125, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera25/foto5.png", 125, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera25/foto6.png", 125, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera25/foto7.png", 125, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");
INSERT INTO foto_camera(path_foto, ref_camera, ref_albergo) VALUES ("media/images/hotelcode/camera25/foto8.png", 125, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia");

INSERT INTO promozione(importo, punti_richiesti, tipo, ref_utente) VALUES (50.0, 1000, 0, NULL); -- 1234
INSERT INTO promozione(importo, punti_richiesti, tipo, ref_utente) VALUES (25.0, 500, 0, NULL); -- 1235
INSERT INTO promozione(importo, punti_richiesti, tipo, ref_utente) VALUES (40.0, 700, 1, NULL); -- 1236
INSERT INTO promozione(importo, punti_richiesti, tipo, ref_utente) VALUES (30.0, 200, 1, NULL); -- 1237
INSERT INTO promozione(importo, punti_richiesti, tipo, ref_utente) VALUES (10.0, 100, 0, NULL); -- 1238
INSERT INTO promozione(importo, punti_richiesti, tipo, ref_utente) VALUES (5.0, 50, 1, NULL); -- 1239
INSERT INTO promozione(importo, punti_richiesti, tipo, ref_utente) VALUES (150.0, 1000, 0, NULL); -- 1240
INSERT INTO promozione(importo, punti_richiesti, tipo, ref_utente) VALUES (125.0, 500, 0, NULL); -- 1241
INSERT INTO promozione(importo, punti_richiesti, tipo, ref_utente) VALUES (140.0, 700, 1, NULL); -- 1242
INSERT INTO promozione(importo, punti_richiesti, tipo, ref_utente) VALUES (130.0, 200, 1, NULL); -- 1243
INSERT INTO promozione(importo, punti_richiesti, tipo, ref_utente) VALUES (110.0, 100, 0, NULL); -- 1244
INSERT INTO promozione(importo, punti_richiesti, tipo, ref_utente) VALUES (15.0, 50, 1, NULL); -- 1245


-- Passate mazza camere 101-125 punti 4.0:
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("mazza", 101, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2022-08-10", "2022-08-11", "11:05:00", "09:50:00", "Esperienza eccezionale. Il personale cortese e disponibile, le camere spaziose e pulite, e la posizione centrale è stata comoda per esplorare la città. La colazione era deliziosa e varia. Consiglio vivamente questo hotel per un soggiorno indimenticabile", 4.0);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("mazza", 102, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2022-09-12", "2022-09-16", "10:30:00", "11:45:00", "Soggiorno piacevole. Camere confortevoli e pulite. Il personale è stato gentile e disponibile. La posizione dell'hotel è ottima, vicino ai principali punti di interesse. Lo consiglio per una visita in questa bellissima città.", 4.1);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("mazza", 103, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2022-07-20", "2022-07-21", "10:15:00", "09:30:00", "Ottimo soggiorno. Le camere sono spaziose e ben arredate. Il personale è stato cortese e professionale. La colazione era abbondante e di ottima qualità. Consiglio vivamente questo hotel a chiunque voglia visitare la città.", 4.2);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("mazza", 104, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2022-10-05", "2022-10-08", "11:00:00", "10:30:00", "Soggiorno meraviglioso. Le camere sono eleganti e pulite. La posizione dell'hotel è fantastica, vicino a tutti i principali luoghi di interesse. Il personale è stato cordiale e disponibile. Consigliato!", 4.3);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("mazza", 105, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2022-11-12", "2022-11-13", "10:45:00", "10:00:00", "Esperienza fantastica. Camere spaziose e confortevoli. Il personale è stato estremamente gentile e disponibile. La colazione era deliziosa. Consiglio vivamente questo hotel per un soggiorno piacevole.", 4.4);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("mazza", 106, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2022-12-25", "2022-12-26", "09:30:00", "10:15:00", "Bellissima esperienza. Camere accoglienti e pulite. Il personale è stato molto cortese e professionale. La posizione dell'hotel è ottima, vicino ai principali punti di interesse. Consigliatissimo!", 4.5);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("mazza", 107, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2023-01-06", "2023-01-09", "10:00:00", "11:30:00", "Soggiorno piacevole. Camere spaziose e ben arredate. Il personale è stato gentile e disponibile. Ottima posizione dell'hotel. La colazione era abbondante. Consiglio vivamente!", 4.6);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("mazza", 108, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2023-02-14", "2023-02-15", "11:30:00", "09:00:00", "Ottimo hotel. Camere pulite e confortevoli. Il personale è stato gentile e attento alle nostre esigenze. La posizione dell'hotel è comoda per visitare la città. Buona colazione. Consigliato!", 4.7);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("mazza", 109, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2023-03-22", "2023-03-27", "09:45:00", "10:45:00", "Esperienza eccezionale. Camere spaziose e ben arredate. Il personale è stato estremamente gentile e disponibile. La posizione dell'hotel è ottima, vicino ai principali punti di interesse. Consigliatissimo!", 4.8);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("mazza", 110, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2023-04-02", "2023-04-03", "10:15:00", "11:15:00", "Soggiorno fantastico. Le camere sono spaziose e pulite. Il personale è stato cortese e professionale. Ottima posizione dell'hotel. Colazione abbondante e gustosa. Consigliato!", 4.9);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("mazza", 111, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2023-05-10", "2023-05-11", "11:00:00", "10:30:00", "Soggiorno meraviglioso. Camere eleganti e confortevoli. Il personale è stato cordiale e disponibile. La colazione era deliziosa. Consiglio vivamente questo hotel per un soggiorno piacevole.", 3.9);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("mazza", 112, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2023-06-21", "2023-06-22", "10:30:00", "09:45:00", "Esperienza fantastica. Camere spaziose e pulite. Il personale è stato estremamente gentile e disponibile. La colazione era abbondante e di ottima qualità. Consiglio vivamente questo hotel a chiunque voglia visitare la città.", 3.8);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("mazza", 113, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2022-08-11", "2022-08-14", "10:45:00", "11:00:00", "Ottimo soggiorno. Le camere sono spaziose e ben arredate. Il personale è stato gentile e disponibile. La posizione dell'hotel è ottima, vicino ai principali punti di interesse. Lo consiglio per una visita in questa bellissima città.", 3.7);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("mazza", 114, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2022-09-16", "2022-09-17", "11:00:00", "10:15:00", "Soggiorno piacevole. Camere confortevoli e pulite. Il personale è stato cortese e professionale. La posizione dell'hotel è fantastica, vicino a tutti i principali luoghi di interesse. Consigliato!", 3.6);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("mazza", 115, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2022-07-21", "2022-07-22", "10:15:00", "09:30:00", "Esperienza eccezionale. Camere spaziose e ben arredate. Il personale è stato estremamente gentile e disponibile. La colazione era deliziosa. Consiglio vivamente questo hotel per un soggiorno indimenticabile.", 3.5);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("mazza", 116, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2022-10-06", "2022-10-07", "10:30:00", "09:45:00", "Bellissima esperienza. Camere accoglienti e pulite. Il personale è stato molto cortese e professionale. La posizione dell'hotel è ottima, vicino ai principali punti di interesse. Consigliatissimo!", 3.4);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("mazza", 117, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2022-11-13", "2022-11-14", "09:45:00", "11:00:00", "Ottimo soggiorno. Le camere sono spaziose e ben arredate. Il personale è stato gentile e disponibile. La posizione dell'hotel è ottima, vicino ai principali punti di interesse. Lo consiglio per una visita in questa bellissima città.", 5.0);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("mazza", 118, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2022-12-26", "2022-12-26", "10:15:00", "09:30:00", "Soggiorno piacevole. Camere confortevoli e pulite. Il personale è stato cortese e professionale. La posizione dell'hotel è fantastica, vicino a tutti i principali luoghi di interesse. Consigliato!", 5.0);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("mazza", 119, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2023-01-09", "2023-01-10", "11:30:00", "10:45:00", "Esperienza fantastica. Camere spaziose e pulite. Il personale è stato estremamente gentile e disponibile. La colazione era abbondante e di ottima qualità. Consiglio vivamente questo hotel a chiunque voglia visitare la città.", 5.0);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("mazza", 120, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2023-02-15", "2023-02-16", "09:30:00", "10:30:00", "Soggiorno meraviglioso. Camere eleganti e confortevoli. Il personale è stato cordiale e disponibile. La colazione era deliziosa. Consiglio vivamente questo hotel per un soggiorno piacevole.", 5.0);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("mazza", 121, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2023-03-23", "2023-03-23", "11:00:00", "10:00:00", "Esperienza eccezionale. Camere spaziose e ben arredate. Il personale è stato estremamente gentile e disponibile. La posizione dell'hotel è ottima, vicino ai principali punti di interesse. Consigliatissimo!", 4.0);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("mazza", 122, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2023-04-03", "2023-04-04", "10:30:00", "09:45:00", "Soggiorno fantastico. Le camere sono spaziose e pulite. Il personale è stato cortese e professionale. Ottima posizione dell'hotel. Colazione abbondante e gustosa. Consigliato!", 4.0);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("mazza", 123, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2023-05-11", "2023-05-12", "09:45:00", "11:00:00", "Ottimo soggiorno. Camere pulite e confortevoli. Il personale è stato gentile e disponibile. La posizione dell'hotel è fantastica, vicino a tutti i principali luoghi di interesse. Lo consiglio per una visita in questa bellissima città.", 4.0);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("mazza", 124, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2023-06-22", "2023-06-23", "10:15:00", "09:30:00", "Esperienza piacevole. Camere accoglienti e pulite. Il personale è stato molto cortese e professionale. La posizione dell'hotel è ottima, vicino ai principali punti di interesse. Consigliatissimo!", 4.0);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("mazza", 125, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2022-08-12", "2022-08-18", "10:45:00", "11:00:00", "Bellissima esperienza. Camere spaziose e ben arredate. Il personale è stato estremamente gentile e disponibile. La colazione era deliziosa. Consiglio vivamente questo hotel per un soggiorno piacevole.", 4.0);

-- Passate gabriele camere 101-125 punti 4.5:
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("gabriele", 101, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2023-06-20", "2023-06-21", "10:05:00", "08:50:00", "Esperienza eccezionale. Il personale cortese e disponibile, le camere spaziose e pulite, e la posizione centrale e' stata comoda per esplorare la citta'. La colazione era deliziosa e varia. Consiglio vivamente questo hotel per un soggiorno indimenticabile", 4.5);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("gabriele", 102, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2023-05-19", "2023-05-19", "11:05:00", "11:50:00", "L'hotel ha superato le mie aspettative. L'atmosfera accogliente, il design elegante e i comfort moderni hanno reso il mio soggiorno molto piacevole. Il personale e' stato estremamente gentile e premuroso, garantendo un servizio di alta qualita'", 4.5);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("gabriele", 103, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2022-06-15", "2022-06-16", "10:30:00", "09:45:00", "Soggiorno piacevole. Camere spaziose e ben arredate. Il personale è stato molto cortese e disponibile. La posizione dell'hotel è ottima, vicino ai principali punti di interesse. Consigliatissimo!", 4.5);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("gabriele", 104, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2022-09-25", "2022-09-26", "09:45:00", "11:00:00", "Ottimo soggiorno. Le camere sono pulite e confortevoli. Il personale è stato gentile e disponibile. La posizione dell'hotel è fantastica, vicino a tutti i principali luoghi di interesse. Lo consiglio per una visita in questa bellissima città.", 4.5);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("gabriele", 105, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2022-12-10", "2022-12-11", "10:15:00", "09:30:00", "Esperienza piacevole. Camere accoglienti e pulite. Il personale è stato molto cortese e professionale. La posizione dell'hotel è ottima, vicino ai principali punti di interesse. Consigliatissimo!", 4.5);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("gabriele", 106, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2023-01-22", "2023-01-23", "11:30:00", "10:45:00", "Esperienza fantastica. Camere spaziose e pulite. Il personale è stato estremamente gentile e disponibile. La colazione era abbondante e di ottima qualità. Consiglio vivamente questo hotel a chiunque voglia visitare la città.", 4.5);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("gabriele", 107, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2023-03-07", "2023-03-08", "09:30:00", "10:30:00", "Soggiorno meraviglioso. Camere eleganti e confortevoli. Il personale è stato cordiale e disponibile. La colazione era deliziosa. Consiglio vivamente questo hotel per un soggiorno piacevole.", 4.5);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("gabriele", 108, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2023-04-16", "2023-04-17", "11:00:00", "10:00:00", "Esperienza eccezionale. Camere spaziose e ben arredate. Il personale è stato estremamente gentile e disponibile. La posizione dell'hotel è ottima, vicino ai principali punti di interesse. Consigliatissimo!", 4.5);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("gabriele", 109, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2023-05-28", "2023-05-29", "10:30:00", "09:45:00", "Soggiorno fantastico. Le camere sono spaziose e pulite. Il personale è stato cortese e professionale. Ottima posizione dell'hotel. Colazione abbondante e gustosa. Consigliato!", 4.5);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("gabriele", 110, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2022-07-11", "2022-07-12", "09:45:00", "11:00:00", "Ottimo soggiorno. Camere pulite e confortevoli. Il personale è stato gentile e disponibile. La posizione dell'hotel è fantastica, vicino a tutti i principali luoghi di interesse. Lo consiglio per una visita in questa bellissima città.", 4.5);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("gabriele", 111, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2022-08-26", "2022-08-27", "10:15:00", "09:30:00", "Esperienza piacevole. Camere accoglienti e pulite. Il personale è stato molto cortese e professionale. La posizione dell'hotel è ottima, vicino ai principali punti di interesse. Consigliatissimo!", 4.5);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("gabriele", 112, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2023-10-03", "2023-10-04", "10:45:00", "11:00:00", "Bellissima esperienza. Camere spaziose e ben arredate. Il personale è stato estremamente gentile e disponibile. La colazione era deliziosa. Consiglio vivamente questo hotel per un soggiorno piacevole.", 4.5);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("gabriele", 113, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2022-11-18", "2022-11-19", "11:30:00", "10:45:00", "Esperienza fantastica. Camere spaziose e pulite. Il personale è stato estremamente gentile e disponibile. La colazione era abbondante e di ottima qualità. Consiglio vivamente questo hotel a chiunque voglia visitare la città.", 4.5);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("gabriele", 114, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2022-12-27", "2022-12-28", "09:30:00", "10:30:00", "Soggiorno meraviglioso. Camere eleganti e confortevoli. Il personale è stato cordiale e disponibile. La colazione era deliziosa. Consiglio vivamente questo hotel per un soggiorno piacevole.", 4.5);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("gabriele", 115, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2022-02-08", "2022-02-09", "11:00:00", "10:00:00", "Esperienza eccezionale. Camere spaziose e ben arredate. Il personale è stato estremamente gentile e disponibile. La posizione dell'hotel è ottima, vicino ai principali punti di interesse. Consigliatissimo!", 4.5);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("gabriele", 116, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2022-03-21", "2022-03-22", "10:30:00", "09:45:00", "Soggiorno fantastico. Le camere sono spaziose e pulite. Il personale è stato cortese e professionale. Ottima posizione dell'hotel. Colazione abbondante e gustosa. Consigliato!", 4.5);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("gabriele", 117, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2023-05-03", "2023-05-04", "09:45:00", "11:00:00", "Ottimo soggiorno. Camere pulite e confortevoli. Il personale è stato gentile e disponibile. La posizione dell'hotel è fantastica, vicino a tutti i principali luoghi di interesse. Lo consiglio per una visita in questa bellissima città.", 4.5);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("gabriele", 118, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2023-06-18", "2023-06-19", "10:15:00", "09:30:00", "Esperienza piacevole. Camere accoglienti e pulite. Il personale è stato molto cortese e professionale. La posizione dell'hotel è ottima, vicino ai principali punti di interesse. Consigliatissimo!", 4.5);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("gabriele", 119, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2022-08-02", "2022-08-03", "10:45:00", "11:00:00", "Bellissima esperienza. Camere spaziose e ben arredate. Il personale è stato estremamente gentile e disponibile. La colazione era deliziosa. Consiglio vivamente questo hotel per un soggiorno piacevole.", 4.5);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("gabriele", 120, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2023-09-15", "2023-09-16", "11:30:00", "10:45:00", "Esperienza fantastica. Camere spaziose e pulite. Il personale è stato estremamente gentile e disponibile. La colazione era abbondante e di ottima qualità. Consiglio vivamente questo hotel a chiunque voglia visitare la città.", 4.5);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("gabriele", 121, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2022-10-29", "2022-10-30", "09:30:00", "10:30:00", "Soggiorno meraviglioso. Camere eleganti e confortevoli. Il personale è stato cordiale e disponibile. La colazione era deliziosa. Consiglio vivamente questo hotel per un soggiorno piacevole.", 4.5);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("gabriele", 122, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2023-12-13", "2023-12-14", "11:00:00", "10:00:00", "Esperienza eccezionale. Camere spaziose e ben arredate. Il personale è stato estremamente gentile e disponibile. La posizione dell'hotel è ottima, vicino ai principali punti di interesse. Consigliatissimo!", 4.5);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("gabriele", 123, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2022-01-27", "2022-01-28", "10:30:00", "09:45:00", "Soggiorno fantastico. Le camere sono spaziose e pulite. Il personale è stato cortese e professionale. Ottima posizione dell'hotel. Colazione abbondante e gustosa. Consigliato!", 4.5);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("gabriele", 124, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2023-03-12", "2023-03-13", "09:45:00", "11:00:00", "Ottimo soggiorno. Camere pulite e confortevoli. Il personale è stato gentile e disponibile. La posizione dell'hotel è fantastica, vicino a tutti i principali luoghi di interesse. Lo consiglio per una visita in questa bellissima città.", 4.5);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("gabriele", 125, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2022-04-25", "2022-04-26", "10:15:00", "09:30:00", "Esperienza piacevole. Camere accoglienti e pulite. Il personale è stato molto cortese e professionale. La posizione dell'hotel è ottima, vicino ai principali punti di interesse. Consigliatissimo!", 4.5);

-- Passate prova camere 101-125 punti 5.0:
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("prova", 101, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2023-06-20", "2023-06-21", "11:05:00", "09:50:00", "Esperienza eccezionale. Il personale cortese e disponibile, le camere spaziose e pulite, e la posizione centrale è stata comoda per esplorare la città. La colazione era deliziosa e varia. Consiglio vivamente questo hotel per un soggiorno indimenticabile", 5.0);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("prova", 102, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2023-05-15", "2023-05-16", "10:30:00", "09:45:00", "Soggiorno piacevole. Camere confortevoli e pulite. Il personale è stato gentile e disponibile. Ottima posizione dell'hotel. Consigliato!", 5.0);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("prova", 103, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2023-04-10", "2023-04-11", "11:15:00", "10:30:00", "Ottima esperienza. Camere spaziose e ben arredate. Personale cordiale e disponibile. Posizione strategica. Colazione abbondante. Consigliato!", 5.0);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("prova", 104, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2023-03-05", "2023-03-06", "09:45:00", "11:00:00", "Soggiorno fantastico. Camere pulite e confortevoli. Personale cortese e disponibile. Ottima posizione. Colazione gustosa. Consigliato!", 5.0);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("prova", 105, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2023-02-20", "2023-02-21", "10:30:00", "09:45:00", "Esperienza eccezionale. Camere spaziose e pulite. Personale cordiale e disponibile. Posizione strategica. Colazione deliziosa. Consigliato!", 5.0);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("prova", 106, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2023-01-15", "2023-01-16", "11:00:00", "10:00:00", "Soggiorno piacevole. Camere confortevoli e ben arredate. Personale gentile e disponibile. Ottima colazione. Consigliato!", 5.0);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("prova", 107, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2022-12-10", "2022-12-11", "09:30:00", "10:45:00", "Esperienza fantastica. Camere spaziose e pulite. Personale cortese e professionale. Posizione comoda. Colazione abbondante. Consigliato!", 5.0);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("prova", 108, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2022-11-05", "2022-11-06", "10:15:00", "11:30:00", "Soggiorno indimenticabile. Camere accoglienti e pulite. Personale gentile e disponibile. Posizione strategica. Ottima colazione. Consigliato!", 5.0);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("prova", 109, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2022-10-20", "2022-10-21", "11:00:00", "10:15:00", "Ottima esperienza. Camere spaziose e ben arredate. Personale cortese. Posizione centrale. Colazione deliziosa. Consigliato!", 5.0);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("prova", 110, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2022-09-15", "2022-09-16", "09:45:00", "11:00:00", "Soggiorno piacevole. Camere pulite e confortevoli. Personale cordiale. Posizione strategica. Colazione gustosa. Consigliato!", 5.0);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("prova", 111, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2022-08-10", "2022-08-11", "10:30:00", "09:45:00", "Esperienza eccezionale. Camere spaziose e pulite. Personale disponibile. Posizione ottima. Colazione abbondante. Consigliato!", 5.0);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("prova", 112, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2022-07-05", "2022-07-06", "11:15:00", "10:30:00", "Soggiorno fantastico. Camere pulite e confortevoli. Personale gentile. Posizione comoda. Colazione deliziosa. Consigliato!", 5.0);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("prova", 113, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2022-06-20", "2022-06-21", "09:45:00", "11:00:00", "Esperienza piacevole. Camere confortevoli e pulite. Personale cordiale. Ottima posizione. Colazione abbondante. Consigliato!", 5.0);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("prova", 114, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2022-05-15", "2022-05-16", "10:30:00", "09:45:00", "Ottima esperienza. Camere spaziose e ben arredate. Personale gentile. Posizione strategica. Colazione deliziosa. Consigliato!", 5.0);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("prova", 115, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2022-04-10", "2022-04-11", "11:00:00", "10:15:00", "Soggiorno fantastico. Camere pulite e confortevoli. Personale cordiale e professionale. Posizione comoda. Colazione deliziosa. Consigliato!", 5.0);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("prova", 116, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2022-03-05", "2022-03-06", "09:45:00", "11:00:00", "Esperienza eccezionale. Camere spaziose e pulite. Personale gentile e disponibile. Posizione centrale. Ottima colazione. Consigliato!", 5.0);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("prova", 117, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2022-02-20", "2022-02-21", "10:30:00", "09:45:00", "Soggiorno piacevole. Camere confortevoli e ben arredate. Personale cordiale e professionale. Posizione strategica. Colazione gustosa. Consigliato!", 5.0);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("prova", 118, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2022-01-15", "2022-01-16", "11:00:00", "10:00:00", "Esperienza fantastica. Camere spaziose e pulite. Personale gentile e disponibile. Posizione ottima. Colazione abbondante. Consigliato!", 5.0);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("prova", 119, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2021-12-10", "2021-12-11", "09:30:00", "10:45:00", "Ottima esperienza. Camere pulite e confortevoli. Personale cordiale. Posizione comoda. Colazione deliziosa. Consigliato!", 5.0);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("prova", 120, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2021-11-05", "2021-11-06", "10:15:00", "11:30:00", "Soggiorno piacevole. Camere spaziose e ben arredate. Personale gentile e disponibile. Posizione strategica. Ottima colazione. Consigliato!", 5.0);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("prova", 121, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2021-10-20", "2021-10-21", "11:00:00", "10:15:00", "Esperienza eccezionale. Camere spaziose e pulite. Personale cortese e professionale. Posizione centrale. Colazione abbondante. Consigliato!", 5.0);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("prova", 122, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2021-09-15", "2021-09-16", "09:45:00", "11:00:00", "Soggiorno fantastico. Camere pulite e confortevoli. Personale cordiale e disponibile. Posizione strategica. Colazione deliziosa. Consigliato!", 5.0);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("prova", 123, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2021-08-10", "2021-08-11", "10:30:00", "09:45:00", "Ottima esperienza. Camere confortevoli e ben arredate. Personale gentile e professionale. Posizione ottima. Colazione gustosa. Consigliato!", 5.0);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("prova", 124, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2021-07-05", "2021-07-06", "11:15:00", "10:30:00", "Esperienza fantastica. Camere spaziose e pulite. Personale cordiale e disponibile. Posizione comoda. Colazione deliziosa. Consigliato!", 5.0);
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione) VALUES ("prova", 125, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2021-06-20", "2021-06-21", "09:45:00", "11:00:00", "Soggiorno piacevole. Camere pulite e confortevoli. Personale gentile. Posizione strategica. Ottima colazione. Consigliato!", 5.0);

-- Presenti:
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo) VALUES ("mazza", 102, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2023-06-30", "2023-07-21", "09:45:00");
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine) VALUES ("mazza", 103, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2023-07-05", "2023-07-05");
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo) VALUES ("gabriele", 118, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2023-07-01", "2023-07-12", "09:45:00");
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine) VALUES ("gabriele", 119, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2023-07-05", "2023-07-05");
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine, check_in_effettivo) VALUES ("prova", 124, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2023-07-20", "2023-07-08", "09:45:00");
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine) VALUES ("prova", 125, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2023-07-05", "2023-07-05");

-- Future:
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine) VALUES ("mazza", 102, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2024-06-30", "2024-07-21");
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine) VALUES ("mazza", 103, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2024-07-05", "2024-07-05");
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine) VALUES ("gabriele", 118, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2024-07-01", "2024-07-12");
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine) VALUES ("gabriele", 119, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2024-07-05", "2024-07-05");
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine) VALUES ("prova", 124, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2024-07-20", "2024-07-08");
INSERT INTO prenotazione(ref_utente, ref_camera, ref_albergo, data_inizio, data_fine) VALUES ("prova", 125, "Via del tutto eccezionale, 42 - Pietrammare 81009, Italia", "2024-07-05", "2024-07-05");