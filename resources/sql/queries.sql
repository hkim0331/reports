-- :name create-upload! :! :n
-- :doc creates a record for uploading
INSERT INTO uploads
(login, filename)
VALUES (:login, :filename)

-- :name get-uploads :? :*
-- :doc retrieve uploads records
SELECT * FROM uploads

-- :name get-logins :? :*
-- :dor retrieve login reverse order
SELECT login from uploads order by id;
