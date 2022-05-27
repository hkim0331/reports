-- :name create-upload! :! :n
-- :doc creates a record for uploading
INSERT INTO uploads
(login, filename)
VALUES (:login, :filename)

-- :name get-uploads :? :*
-- :doc retrieve uploads records
SELECT * FROM uploads

-- :name get-logins :? :*
-- :doc retrieve logins order by id
SELECT login FROM uploads order by id

-- :name logins-by-reverse-uploaded :? :*
-- :doc retrieve logins, newers are first
SELECT login FROM uploads ORDER BY uploaded_at DESC

-- :name save-message! :! :n
-- :doc message from snd to rcv
INSERT INTO goods
(snd, rcv, message)
VALUES (:snd, :rcv, :message)

-- :name rcvs :? :*
-- :doc messages received by rcv
SELECT * FROM goods
WHERE rcv = :rcv order by id desc

-- :name snds :? :*
-- :doc messages sent by snd
SELECT * FROM goods
WHERE snd = :snd order by id desc

-- :name goods :? :*
-- :doc retrieve goods all
SELECT * FROM goods