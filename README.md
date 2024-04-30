# reports

This README.md is awfully insufficient.
Need updates.

情報リテラシー 2020 レポート回収サイト。

generated using Luminus version "4.38"

    % lein new luminus reports +postgres +reagent +auth

## Prerequisites

You will need [Leiningen][1] 2.0 or above installed.

[1]: https://github.com/technomancy/leiningen

## Develop

    % cd reports
    % code .
    % cat dev-config.edn
    % npm install
    % lein repl
    user> (start)

    or in Calva,
    1, choose start Server and Client
    2, open http://localhost:3000 after ensuring shadow-clj launched

### develop on dev-container

(not ready)

## Running

To start a web server for the application, run:

    % lein run

## License

Copyright © 2022,2023,2024 Hiroshi Kimura
