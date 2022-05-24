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
SELECT login from uploads order by id

-- :name logins-by-reverse-uploaded :? :*
-- :doc retrieve logins, newers are first
SELECT login from uploads order by uploaded_at desc
