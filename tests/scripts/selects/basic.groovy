/**
 * @author IWEERARATHNA
 */
[
        $DSL.select {
            TARGET (Film.alias("f"))
            DISTINCT_FETCH (f.title)
        },
        [
            mysql: "SELECT DISTINCT f.title FROM `Film` f"
        ],

        $DSL.select {
            TARGET (Film.alias("f"))
            DISTINCT_FETCH (f.title, f.description)
        },
        [
            mysql: "SELECT DISTINCT f.title, f.description FROM `Film` f"
        ],

        $DSL.select {
            TARGET (Film.alias("f"))
            FETCH (f.rental_duration, COUNT())
            GROUP_BY (f.rental_duration)
            HAVING {
                GT (COUNT(), 200)
            }
        },
        [
            mysql: "SELECT f.rental_duration, COUNT(*) FROM `Film` f GROUP BY f.rental_duration HAVING COUNT(*) > 200"
        ],

        $DSL.select {
            TARGET (Film.alias("f"))
            FETCH (f.rental_duration, COUNT().alias("total"))
            GROUP_BY (f.rental_duration)
            HAVING {
                GT (total, 200)
            }
        },
        [
            mysql: "SELECT f.rental_duration, COUNT(*) AS total FROM `Film` f GROUP BY f.rental_duration HAVING total > 200"
        ],

        $DSL.select {
            TARGET (Film.alias("f"))
            FETCH (COLUMN("rental_duration"), COUNT().alias("total"))
            GROUP_BY (f.rental_duration)
            HAVING {
                GT (total, 200)
            }
        },
        [
            mysql: "SELECT f.rental_duration, COUNT(*) AS total FROM `Film` f GROUP BY f.rental_duration HAVING total > 200"
        ]
]