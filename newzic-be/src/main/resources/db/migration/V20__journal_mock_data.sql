-- ═══════════════════════════════════════════════════════════
-- JOURNAL MOCK DATA — 20+ realistic music journal posts
-- Uses existing demo users from seed data
-- ═══════════════════════════════════════════════════════════

-- We reference users by their known UUIDs from the seed data.
-- First, let's get some user IDs via a DO block.

DO $$
DECLARE
    uid1 UUID; uid2 UUID; uid3 UUID; uid4 UUID; uid5 UUID;
    p1 UUID; p2 UUID; p3 UUID; p4 UUID; p5 UUID;
    p6 UUID; p7 UUID; p8 UUID; p9 UUID; p10 UUID;
    p11 UUID; p12 UUID; p13 UUID; p14 UUID; p15 UUID;
    p16 UUID; p17 UUID; p18 UUID; p19 UUID; p20 UUID;
    p21 UUID; p22 UUID;
BEGIN
    -- Get first 5 users (skip admin)
    SELECT id INTO uid1 FROM users WHERE username != 'admin' ORDER BY created_at LIMIT 1 OFFSET 0;
    SELECT id INTO uid2 FROM users WHERE username != 'admin' ORDER BY created_at LIMIT 1 OFFSET 1;
    SELECT id INTO uid3 FROM users WHERE username != 'admin' ORDER BY created_at LIMIT 1 OFFSET 2;
    SELECT id INTO uid4 FROM users WHERE username != 'admin' ORDER BY created_at LIMIT 1 OFFSET 3;
    SELECT id INTO uid5 FROM users WHERE username != 'admin' ORDER BY created_at LIMIT 1 OFFSET 4;

    -- If we don't have 5 users, exit gracefully
    IF uid1 IS NULL OR uid2 IS NULL THEN
        RETURN;
    END IF;
    IF uid3 IS NULL THEN uid3 := uid1; END IF;
    IF uid4 IS NULL THEN uid4 := uid2; END IF;
    IF uid5 IS NULL THEN uid5 := uid1; END IF;

    -- Generate UUIDs for posts
    p1 := gen_random_uuid(); p2 := gen_random_uuid(); p3 := gen_random_uuid();
    p4 := gen_random_uuid(); p5 := gen_random_uuid(); p6 := gen_random_uuid();
    p7 := gen_random_uuid(); p8 := gen_random_uuid(); p9 := gen_random_uuid();
    p10 := gen_random_uuid(); p11 := gen_random_uuid(); p12 := gen_random_uuid();
    p13 := gen_random_uuid(); p14 := gen_random_uuid(); p15 := gen_random_uuid();
    p16 := gen_random_uuid(); p17 := gen_random_uuid(); p18 := gen_random_uuid();
    p19 := gen_random_uuid(); p20 := gen_random_uuid(); p21 := gen_random_uuid();
    p22 := gen_random_uuid();

    -- Clean up any data from a previous failed/partial run
    DELETE FROM journal_comments;
    DELETE FROM journal_reactions;
    DELETE FROM journal_post_tagged_users;
    DELETE FROM journal_post_hashtags;
    DELETE FROM journal_posts;

    -- ── UPDATE posts ──
    INSERT INTO journal_posts (id, author_id, content, category, reaction_count, comment_count, created_at, updated_at) VALUES
    (p1, uid1, 'Sto lavorando a un nuovo EP! 5 tracce, tutte prodotte in casa. Non vedo l''ora di farvelo sentire 🎧 #newmusic #ep #producer', 'UPDATE', 12, 3, NOW() - INTERVAL '2 hours', NOW() - INTERVAL '2 hours'),
    (p2, uid2, 'Mix finale completato. Dopo 3 mesi di lavoro, il singolo è pronto. Mastering la prossima settimana 🔥 #mixing #newrelease', 'UPDATE', 8, 2, NOW() - INTERVAL '5 hours', NOW() - INTERVAL '5 hours'),
    (p3, uid3, 'Ho appena scritto il testo più personale della mia carriera. A volte la musica è terapia. #songwriting #music', 'UPDATE', 15, 5, NOW() - INTERVAL '8 hours', NOW() - INTERVAL '8 hours'),
    (p4, uid4, 'Studio session alle 3 di notte. Le migliori idee arrivano sempre a quest''ora 🌙 #latenightvibes #studio', 'UPDATE', 6, 1, NOW() - INTERVAL '12 hours', NOW() - INTERVAL '12 hours'),
    (p5, uid5, 'Finalmente ho aggiornato il mio setup! Nuove casse monitor e trattamento acustico. Il suono è un altro mondo 🔊 #homestudio #producer', 'UPDATE', 9, 2, NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day');

    -- ── COLLABORATION posts ──
    INSERT INTO journal_posts (id, author_id, content, category, reaction_count, comment_count, created_at, updated_at) VALUES
    (p6, uid1, 'Session incredibile oggi! Abbiamo registrato 3 tracce in un pomeriggio. La chimica musicale è tutto 🎵 #collaboration #recording', 'COLLABORATION', 18, 7, NOW() - INTERVAL '3 hours', NOW() - INTERVAL '3 hours'),
    (p7, uid2, 'Nuovo progetto con un producer pazzesco. Non posso ancora dire chi è, ma preparatevi 👀 #comingsoon #newproject', 'COLLABORATION', 22, 9, NOW() - INTERVAL '6 hours', NOW() - INTERVAL '6 hours'),
    (p8, uid3, 'Feat confermato! Il brano esce il mese prossimo. Due mondi musicali completamente diversi che si incontrano 🤝 #feat #newmusic', 'COLLABORATION', 14, 4, NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),
    (p9, uid4, 'Prima volta in uno studio professionale per registrare le chitarre. Esperienza incredibile 🎸 #recording #guitar', 'COLLABORATION', 7, 2, NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days');

    -- ── LOOKING_FOR_COLLAB posts ──
    INSERT INTO journal_posts (id, author_id, content, category, reaction_count, comment_count, created_at, updated_at) VALUES
    (p10, uid1, 'Cerco una cantante pop/R&B per un progetto estivo. Ho 4 basi pronte, serve solo la voce giusta! DM aperto 🎤 #lookingforcollab #vocalist #pop', 'LOOKING_FOR_COLLAB', 11, 6, NOW() - INTERVAL '4 hours', NOW() - INTERVAL '4 hours'),
    (p11, uid5, 'Producer trap/drill cercasi! Ho dei testi pronti ma mi serve qualcuno che sappia creare l''atmosfera giusta 🔥 #producer #trap #drill', 'LOOKING_FOR_COLLAB', 5, 3, NOW() - INTERVAL '10 hours', NOW() - INTERVAL '10 hours'),
    (p12, uid3, 'Band completa cerca batterista per live session a Milano. Genere: indie rock / alternative. Scrivetemi! 🥁 #band #drummer #milano', 'LOOKING_FOR_COLLAB', 8, 4, NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),
    (p13, uid2, 'Qualcuno sa suonare il violino e vuole provare qualcosa di sperimentale? Ho un''idea folle 🎻 #experimental #violin #collab', 'LOOKING_FOR_COLLAB', 13, 5, NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days');

    -- ── ANNOUNCEMENT posts ──
    INSERT INTO journal_posts (id, author_id, content, category, reaction_count, comment_count, created_at, updated_at) VALUES
    (p14, uid1, '🚨 NUOVO SINGOLO FUORI VENERDÌ! "Notti di Vetro" — il brano più atteso dell''anno. Pre-save link in bio! #newrelease #single', 'ANNOUNCEMENT', 25, 12, NOW() - INTERVAL '1 hour', NOW() - INTERVAL '1 hour'),
    (p15, uid4, 'LIVE il 15 luglio al Jazz Club di Roma! Primo concerto dopo 2 anni. Biglietti disponibili da domani 🎪 #live #concert #roma', 'ANNOUNCEMENT', 20, 8, NOW() - INTERVAL '7 hours', NOW() - INTERVAL '7 hours'),
    (p16, uid5, 'Ho raggiunto i 10.000 ascolti su Newzic! Grazie a tutti voi, non ci credo ancora ❤️ #milestone #thankyou #10k', 'ANNOUNCEMENT', 30, 15, NOW() - INTERVAL '9 hours', NOW() - INTERVAL '9 hours'),
    (p17, uid2, 'Il mio primo album ufficiale esce il 1° agosto. 12 tracce. 2 anni di lavoro. "Origini" 💿 #album #origini #debut', 'ANNOUNCEMENT', 35, 20, NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days');

    -- ── More UPDATE posts for variety ──
    INSERT INTO journal_posts (id, author_id, content, category, reaction_count, comment_count, created_at, updated_at) VALUES
    (p18, uid3, 'Giorno 47 di produzione dell''album. Oggi ho rifatto completamente una traccia da zero. A volte bisogna avere il coraggio di ricominciare 💪 #albumdiary #production', 'UPDATE', 10, 3, NOW() - INTERVAL '14 hours', NOW() - INTERVAL '14 hours'),
    (p19, uid1, 'Ho imparato a usare un nuovo synth analogico. I suoni che tira fuori sono incredibili, sembra di stare negli anni 80 🎹 #synth #analog #producer', 'UPDATE', 7, 1, NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'),
    (p20, uid4, 'Feedback dal mio mentore: "Stai migliorando, ma devi osare di più". Ha ragione. Il prossimo brano sarà diverso da tutto quello che ho fatto 🚀 #growth #music', 'UPDATE', 4, 2, NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),
    (p21, uid5, 'Piccola vittoria: oggi una radio locale ha trasmesso il mio brano! Sembra poco ma per me è enorme 📻 #radio #indie #milestone', 'ANNOUNCEMENT', 16, 6, NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days'),
    (p22, uid2, 'Provando nuovi effetti sulla voce. Il vocoder è diventato il mio nuovo migliore amico 🤖🎤 #vocoder #experimental #vocals', 'UPDATE', 5, 1, NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days');

    -- ── Hashtags ──
    INSERT INTO journal_post_hashtags (post_id, hashtag) VALUES
    (p1, 'newmusic'), (p1, 'ep'), (p1, 'producer'),
    (p2, 'mixing'), (p2, 'newrelease'),
    (p3, 'songwriting'), (p3, 'music'),
    (p4, 'latenightvibes'), (p4, 'studio'),
    (p5, 'homestudio'), (p5, 'producer'),
    (p6, 'collaboration'), (p6, 'recording'),
    (p7, 'comingsoon'), (p7, 'newproject'),
    (p8, 'feat'), (p8, 'newmusic'),
    (p9, 'recording'), (p9, 'guitar'),
    (p10, 'lookingforcollab'), (p10, 'vocalist'), (p10, 'pop'),
    (p11, 'producer'), (p11, 'trap'), (p11, 'drill'),
    (p12, 'band'), (p12, 'drummer'), (p12, 'milano'),
    (p13, 'experimental'), (p13, 'violin'), (p13, 'collab'),
    (p14, 'newrelease'), (p14, 'single'),
    (p15, 'live'), (p15, 'concert'), (p15, 'roma'),
    (p16, 'milestone'), (p16, 'thankyou'), (p16, '10k'),
    (p17, 'album'), (p17, 'origini'), (p17, 'debut'),
    (p18, 'albumdiary'), (p18, 'production'),
    (p19, 'synth'), (p19, 'analog'), (p19, 'producer'),
    (p20, 'growth'), (p20, 'music'),
    (p21, 'radio'), (p21, 'indie'), (p21, 'milestone'),
    (p22, 'vocoder'), (p22, 'experimental'), (p22, 'vocals');

    -- ── Tagged users (some posts tag other users) ──
    INSERT INTO journal_post_tagged_users (post_id, user_id) VALUES
    (p6, uid2), (p6, uid3),
    (p7, uid1),
    (p8, uid4)
    ON CONFLICT DO NOTHING;

    -- ── Some reactions ──
    INSERT INTO journal_reactions (id, user_id, post_id, type, created_at) VALUES
    (gen_random_uuid(), uid2, p1, 'FIRE', NOW() - INTERVAL '1 hour'),
    (gen_random_uuid(), uid3, p1, 'LIKE', NOW() - INTERVAL '1 hour'),
    (gen_random_uuid(), uid4, p1, 'HYPE', NOW() - INTERVAL '30 minutes'),
    (gen_random_uuid(), uid1, p2, 'FIRE', NOW() - INTERVAL '4 hours'),
    (gen_random_uuid(), uid3, p2, 'MUSIC', NOW() - INTERVAL '3 hours'),
    (gen_random_uuid(), uid1, p3, 'LIKE', NOW() - INTERVAL '7 hours'),
    (gen_random_uuid(), uid2, p3, 'LIKE', NOW() - INTERVAL '6 hours'),
    (gen_random_uuid(), uid5, p3, 'FIRE', NOW() - INTERVAL '5 hours'),
    (gen_random_uuid(), uid1, p7, 'HYPE', NOW() - INTERVAL '5 hours'),
    (gen_random_uuid(), uid3, p7, 'FIRE', NOW() - INTERVAL '4 hours'),
    (gen_random_uuid(), uid4, p7, 'LIKE', NOW() - INTERVAL '4 hours'),
    (gen_random_uuid(), uid5, p7, 'MUSIC', NOW() - INTERVAL '3 hours'),
    (gen_random_uuid(), uid1, p14, 'HYPE', NOW() - INTERVAL '30 minutes'),
    (gen_random_uuid(), uid2, p14, 'FIRE', NOW() - INTERVAL '30 minutes'),
    (gen_random_uuid(), uid3, p14, 'LIKE', NOW() - INTERVAL '20 minutes'),
    (gen_random_uuid(), uid4, p14, 'MUSIC', NOW() - INTERVAL '15 minutes'),
    (gen_random_uuid(), uid5, p14, 'HYPE', NOW() - INTERVAL '10 minutes'),
    (gen_random_uuid(), uid1, p16, 'LIKE', NOW() - INTERVAL '8 hours'),
    (gen_random_uuid(), uid2, p16, 'FIRE', NOW() - INTERVAL '8 hours'),
    (gen_random_uuid(), uid3, p16, 'HYPE', NOW() - INTERVAL '7 hours'),
    (gen_random_uuid(), uid4, p16, 'LIKE', NOW() - INTERVAL '7 hours'),
    (gen_random_uuid(), uid1, p17, 'FIRE', NOW() - INTERVAL '1 day'),
    (gen_random_uuid(), uid3, p17, 'HYPE', NOW() - INTERVAL '1 day'),
    (gen_random_uuid(), uid4, p17, 'LIKE', NOW() - INTERVAL '1 day'),
    (gen_random_uuid(), uid5, p17, 'MUSIC', NOW() - INTERVAL '1 day')
    ON CONFLICT DO NOTHING;

    -- ── Some comments ──
    INSERT INTO journal_comments (id, post_id, author_id, content, created_at) VALUES
    (gen_random_uuid(), p1, uid2, 'Non vedo l''ora! 🔥', NOW() - INTERVAL '1 hour'),
    (gen_random_uuid(), p1, uid3, 'Se hai bisogno di un featuring fammi sapere!', NOW() - INTERVAL '45 minutes'),
    (gen_random_uuid(), p3, uid1, 'La musica più vera nasce dalle emozioni. Forza! ❤️', NOW() - INTERVAL '7 hours'),
    (gen_random_uuid(), p3, uid4, 'Capisco perfettamente. Scrivere è liberarsi.', NOW() - INTERVAL '6 hours'),
    (gen_random_uuid(), p7, uid5, 'Chi è?? Dai dicci qualcosa! 👀', NOW() - INTERVAL '5 hours'),
    (gen_random_uuid(), p7, uid1, 'Hype assurdo!', NOW() - INTERVAL '4 hours'),
    (gen_random_uuid(), p10, uid2, 'Ti ho scritto in DM! Ho la voce che cerchi 🎤', NOW() - INTERVAL '3 hours'),
    (gen_random_uuid(), p10, uid5, 'Conosco una cantante bravissima, te la presento!', NOW() - INTERVAL '2 hours'),
    (gen_random_uuid(), p14, uid3, 'FINALMENTE! Pre-save fatto subito 🙌', NOW() - INTERVAL '50 minutes'),
    (gen_random_uuid(), p14, uid4, 'Il titolo è bellissimo', NOW() - INTERVAL '40 minutes'),
    (gen_random_uuid(), p14, uid5, 'Countdown iniziato! 🔥🔥🔥', NOW() - INTERVAL '30 minutes'),
    (gen_random_uuid(), p16, uid1, 'Te lo meriti tutto! Congratulazioni ❤️', NOW() - INTERVAL '8 hours'),
    (gen_random_uuid(), p16, uid2, 'Prossimo obiettivo: 100K! 🚀', NOW() - INTERVAL '7 hours'),
    (gen_random_uuid(), p17, uid1, 'Due anni di lavoro si sentiranno. Non vedo l''ora!', NOW() - INTERVAL '1 day'),
    (gen_random_uuid(), p17, uid4, '12 tracce? Album vero! Rispetto 💪', NOW() - INTERVAL '1 day')
    ON CONFLICT DO NOTHING;

END $$;
