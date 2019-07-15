.PHONY: fig jar test deps

test:
	clojure -A:test

fig: deps
	clojure -A:figwheel

jar: deps
	rm -rf resources/public/cljs out
	clojure -A:cljsbuild
	clojure -A:pack

deps: resources/deps/calendar.min.js resources/public/css/calendar.min.css

resources/deps/calendar.min.js:
	curl -L -o resources/deps/calendar.min.js https://github.com/mdehoog/Semantic-UI-Calendar/raw/master/dist/calendar.min.js

resources/public/css/calendar.min.css:
	curl -L -o resources/public/css/calendar.min.css https://github.com/mdehoog/Semantic-UI-Calendar/raw/master/dist/calendar.min.css
