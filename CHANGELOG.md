# Reports

## Unreleased
- Uploads
- Browse
- Goods
- home-routes
- app-routes after login success
- login.html css

## 0.3.0 - 2022-05-23
- (assoc :session {}) は良くて、(dissoc :session) はダメな理由はなんだ？
  :session キーがないのがダメってこと？

# RESTART

## 0.2.1 - 2022-05-22
- early deploy
- (hato.client/get url {:as :json})
- I don't believe CORS.
  リバースプロキシ配下の web app のアクセスを許すのに次はまずいんじゃないの？
  せめて表向きのグローバルアドレスでフィルタすべき。誤解しているか？

  :access-control-allow-origin  [#"http://localhost.*"]

## 0.2.0 - 2022-05-22
- forgot buddy. copied from other project created by
  `lein new luminus app +budy`
- login/logout. when login error, flash message

## 0.1.0  - 2022-05-22
prep for development.
- lein new luminus reports +reagent +postgres
- antq --upgrade
- npm install
- npm install xmlhttprequest
- code REPL report Server+Client
- create database reports owner='postgres';
- (create-migration "uploads")
- (create-migration "goods")
- (migrate)
- gh repo create hkim0331/reports.git --public
- git remote add origin git@github.com:hkim0331/reports.git