.PHONY: fig jar

fig:
	clojure -A:figwheel

jar:
	rm -rf resources/public/cljs out
	clojure -A:cljsbuild
	clojure -A:pack

