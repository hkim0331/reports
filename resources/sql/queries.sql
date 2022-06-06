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

----------------------------
-- goods
----------------------------

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

-----------------------------------
-- titles
-----------------------------------
-- :name titles
-- :doc fetch all titles
SELECT * FROM titles

-- :name insert-title! :! :n
-- :doc insert login's report title
INSERT INTO titles
(login, title)
VALUES (:login, :title)

-- :name update-title! :! :n
-- :doc update login's existing report title
UPDATE titles SET title = :title
WHERE login = :login

-- :name find-title :? :1
-- :doc find login's report title
SELECT * FROM titles
WHERE login = :login

---------------------
-- upload records
---------------------

-- :name records :? :*
-- :doc fetch upload count by date
SELECT DATE(uploaded_at), COUNT(*)
FROM uploads
GROUP BY DATE(uploaded_at) ORDER BY DATE(uploaded_at)

-- :name record :? :*
-- :doc fetch `login`s upload count by date
SELECT DATE(uploaded_at), COUNT(*)
FROM uploads
WHERE login = :login
GROUP BY DATE(uploaded_at) ORDER BY DATE(uploaded_at)
