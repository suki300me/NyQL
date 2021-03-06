/**
 * @author IWEERARATHNA
 */

def innQ = $DSL.select {
    TARGET (Film.alias("f"))
}

def innQAny = $DSL.select {
    TARGET (Ages.alias("ag"))
    FETCH (ag.age)
    WHERE {
        EQ (ag.group, PARAM("groupId"))
    }
}

[
    $DSL.select {
        TARGET (Film.alias("f"))
        FETCH ()
        WHERE {
            EQ (f.film_id, 123)
            AND
            NEQ (f.title, STR("ACE GOLDFINDER"))
            AND
            IN (f.release_year, 2006)
            OR
            IN (f.language_id, PARAM("langId"))
        }
    },
    [
        mysql: ["SELECT * FROM `Film` f " +
                "WHERE f.film_id = 123 AND f.title <> \"ACE GOLDFINDER\" AND f.release_year IN (2006) OR f.language_id IN (?)",
                ["langId"]]
    ],

    $DSL.select {
        TARGET (Film.alias("f"))
        FETCH ()
        WHERE {
            ALL {
                EQ (f.film_id, 123)
                NEQ (f.title, STR("ACE GOLDFINDER"))
            }
            AND
            GT (f.release_year, 2004)
            OR
            GTE (f.rental_duration, PARAM("y2017"))
        }
    },
    [
        mysql: ["SELECT * FROM `Film` f " +
                "WHERE (f.film_id = 123 AND f.title <> \"ACE GOLDFINDER\") AND f.release_year > 2004 OR f.rental_duration >= ?",
                ["y2017"]]
    ],

    $DSL.select {
        TARGET (Film.alias("f"))
        FETCH ()
        WHERE {
            ANY {
                EQ (f.film_id, 123)
                NEQ (f.title, STR("AIRPLANE SIERRA"))
            }
            AND
            LT (f.release_year, 2015)
            OR {
                LTE(f.length, PARAM("y2017"))
            }
        }
    },
    [
        mysql: ["SELECT * FROM `Film` f " +
                "WHERE (f.film_id = 123 OR f.title <> \"AIRPLANE SIERRA\") AND f.release_year < 2015 OR f.length <= ?",
                ["y2017"]]
    ],

    $DSL.select {
        TARGET (Film.alias("f"))
        FETCH ()
        WHERE {
            EQ (1, 1)
            AND
            ALL {
                EQ (f.film_id, 123)
                NEQ (f.title, STR("POND SEATTLE"))
            }
            AND
            NOTNULL (f.rating)
            AND
            ISNULL (f.original_language_id)
        }
    },
    [
        mysql: "SELECT * FROM `Film` f " +
                "WHERE 1 = 1 " +
                "AND (f.film_id = 123 AND f.title <> \"POND SEATTLE\") " +
                "AND f.rating IS NOT NULL AND f.original_language_id IS NULL"
    ],

    $DSL.select {
        TARGET (Film.alias("f"))
        FETCH ()
        WHERE {
            NEQ (2, 1)
            AND
            ANY {
                EQ (f.film_id, 123)
                NEQ (f.title, STR("MOULIN WAKE"))
            }
            AND {
                LIKE(f.description, STR("%Dragon%"))
            }
            AND
            NOTLIKE (f.title, STR("%Fairy"))
        }
    },
    [
        mysql: "SELECT * FROM `Film` f " +
            "WHERE 2 <> 1 AND (f.film_id = 123 OR f.title <> \"MOULIN WAKE\") AND f.description LIKE \"%Dragon%\" AND f.title NOT LIKE \"%Fairy\""
    ],

    $DSL.select {
        TARGET (Film.alias("f"))
        FETCH ()
        WHERE {
            BETWEEN (f.rental_rate, 2.99, 5.0)
            AND
            GT (f.film_id, 1)
        }
    },
    [
        mysql: "SELECT * FROM `Film` f WHERE f.rental_rate BETWEEN 2.99 AND 5.0 AND f.film_id > 1"
    ],

    $DSL.select {
        TARGET (Film.alias("f"))
        FETCH ()
        WHERE {
            NOTBETWEEN (f.rental_rate, 2.99, 5.0)
            AND (NOTIN (f.language_id, 1))
        }
    },
    [
        mysql: "SELECT * FROM `Film` f WHERE f.rental_rate NOT BETWEEN 2.99 AND 5.0 AND f.language_id NOT IN (1)"
    ],

    $DSL.select {
        TARGET (Film.alias("f"))
        FETCH ()
        WHERE {
            NOTBETWEEN (f.rental_rate, 2.99, 5.0)
            OR (IN (f.language_id, PARAMLIST("singleList")))
        }
    },
    [
        mysql: ["SELECT * FROM `Film` f " +
                "WHERE f.rental_rate NOT BETWEEN 2.99 AND 5.0 OR f.language_id IN (::singleList::)", ["singleList"]]
    ],

    $DSL.select {
        TARGET (Film.alias("f"))
        FETCH ()
        WHERE {
            NOTBETWEEN (f.rental_rate, 2.99, 5.0)
            AND (IN (f.language_id, PARAMLIST("doubleList")))
        }
    },
    [
        mysql: ["SELECT * FROM `Film` f " +
                "WHERE f.rental_rate NOT BETWEEN 2.99 AND 5.0 AND f.language_id IN (::doubleList::)", ["doubleList"]]
    ],

    $DSL.select {
        TARGET (Film.alias("f"))
        FETCH ()
        WHERE {
            EQ (f.language_id, null)
            AND
            NEQ (f.language_id, null)
        }
    },
    [
        mysql: "SELECT * FROM `Film` f WHERE f.language_id IS NULL AND f.language_id IS NOT NULL"
    ],

    $DSL.select {
        TARGET (Film.alias("f"))
        FETCH ()
        WHERE {
            NEQ (f.title, STR("ACE GOLDFINDER"))
            AND
            IN (f.release_year, $SESSION.emptyList)
        }
    },
    [
        mysql: "SELECT * FROM `Film` f WHERE f.title <> \"ACE GOLDFINDER\" AND f.release_year IN (NULL)"
    ],

    $DSL.select {
        TARGET (Film.alias("f"))
        FETCH ()
        WHERE {
            NEQ (f.title, STR("ACE GOLDFINDER"))
            AND
            NIN (f.release_year, $SESSION.emptyList)
        }
    },
    [
        mysql: "SELECT * FROM `Film` f WHERE f.title <> \"ACE GOLDFINDER\" AND f.release_year NOT IN (NULL)"
    ],

    $DSL.select {
        TARGET (Address.alias("ad"))
        FETCH ()
        WHERE {
            EQ (ad.city_id, ad.city_id + 1)
        }
    },
    [
        mysql: "SELECT * FROM `Address` ad WHERE ad.city_id = (ad.city_id + 1)"
    ],

    $DSL.select {
        TARGET (Film.alias("f"))
        FETCH ()
        WHERE {
            LIKE (f.description, CONCAT(STR("%"), POSITION(f.title, STR("a")), STR("-%")))
        }
    },
    [
        mysql: "SELECT * FROM `Film` f WHERE f.description LIKE CONCAT(\"%\", POSITION(\"a\" IN f.title), \"-%\")"
    ],

    $DSL.select {
        TARGET (Address.alias("ad"))
        WHERE {
            EXISTS (innQ)
            AND
            NOTEXISTS (innQ)
        }
    },
    [
        mysql: "SELECT * FROM `Address` ad WHERE EXISTS (SELECT * FROM `Film` f) AND NOT EXISTS (SELECT * FROM `Film` f)"
    ],

    $DSL.select {
        TARGET (Address.alias("ad"))
        WHERE {
            ISNULL (TABLE(QUERY {
                TARGET (Film.alias('f'))
            }))
        }
    },
    [
            mysql: "SELECT * FROM `Address` ad WHERE (SELECT * FROM `Film` f) IS NULL"
    ],

    $DSL.select {
        TARGET (Address.alias("ad"))
        WHERE {
            GT (TABLE(innQ), 5)
        }
    },
    [
            mysql: "SELECT * FROM `Address` ad WHERE (SELECT * FROM `Film` f) > 5"
    ],

    $DSL.select {
        TARGET (Film.alias("f"))
        FETCH ()
        WHERE {
            NEQ (f.title, STR("ACE GOLDFINDER"))
            AND
            IN (f.release_year, PARAMLIST("years"))
        }
    },
    [
        mysql: ["SELECT * FROM `Film` f " +
                "WHERE f.title <> \"ACE GOLDFINDER\" AND f.release_year IN (::years::)", ["years"]]
    ],

    $DSL.select {
        TARGET (Film.alias("f"))
        WHERE {
            EQ (f.description, IFNOTNULL(f.alternative_title, STR("")))
        }
    },
    [
        mysql: "SELECT * FROM `Film` f WHERE f.description = CASE WHEN f.alternative_title IS NOT NULL THEN \"\" ELSE f.alternative_title END"
    ],

    $DSL.select {
        TARGET (Film.alias("f"))
        WHERE {
            AND (EQ (f.description, STR("hello")))
        }
    },
    [
        mysql: "SELECT * FROM `Film` f WHERE  AND f.description = \"hello\""
    ],

    $DSL.select {
        TARGET (Film.alias("f"))
        FETCH ()
        WHERE {
            ALL {
                EQ (f.film_id, 123)
                GT (f.releaseDate, PARAM_DATE("relDate"))
                GT (f.debutTime, PARAM_TIMESTAMP("debutTime"))
            }
            OR
            IN (f.language_id, PARAM("langId"))
            OR
            EQ (f.debutTime2, PARAM_TIMESTAMP("debutTime2", "YYYY-MM-DDThh:mm:ssTZD"))
        }
    },
    [
        mysql: ["SELECT * FROM `Film` f " +
                "WHERE (f.film_id = 123 AND f.releaseDate > ? AND f.debutTime > ?) OR f.language_id IN (?) OR f.debutTime2 = ?",
                ["relDate", "debutTime", "langId", "debutTime2"]]
    ],

    $DSL.select {
        TARGET (Film.alias("f"))
        WHERE {
            EQ (f.id, CASE {
                WHEN {
                    EQ (f.lastId, PARAM("whereParam"))
                }
                THEN {
                    NUM(0)
                }
                ELSE {
                    PARAM("elseParam")
                }
            })
        }
    },
    [
            mysql: ["SELECT * FROM `Film` f WHERE f.id = CASE WHEN f.lastId = ? THEN 0 ELSE ? END", ["whereParam", "elseParam"]]
    ],

    $DSL.select {
        TARGET (Actor.alias("ac"))
        WHERE {
            GT (ac.age, ANY(QUERY {
                            TARGET (Ages.alias("ag"))
                            FETCH (ag.age)
                        }))
        }
    },
    [
            mysql: "SELECT * FROM `Actor` ac WHERE ac.age > ANY (SELECT ag.age FROM `Ages` ag)"
    ],

    $DSL.select {
        TARGET (Actor.alias("ac"))
        WHERE {
            GT (ac.age, ANY(QUERY {
                TARGET (Ages.alias("ag"))
                FETCH (ag.age)
                WHERE {
                    EQ (ag.group, PARAM("groupId"))
                }
            }))
            AND
            EQ (ac.birthYear, PARAM("birthYear"))
        }
    },
    [
            mysql: ["SELECT * FROM `Actor` ac WHERE ac.age > ANY (SELECT ag.age FROM `Ages` ag WHERE ag.group = ?) " +
                            "AND ac.birthYear = ?",
                    ["groupId", "birthYear"]]
    ],

    $DSL.select {
        TARGET (Actor.alias("ac"))
        WHERE {
            GT (ac.age, ALL(innQAny))
            AND
            EQ (ac.birthYear, PARAM("birthYear"))
        }
    },
    [
            mysql: ["SELECT * FROM `Actor` ac WHERE ac.age > ALL (SELECT ag.age FROM `Ages` ag WHERE ag.group = ?) " +
                            "AND ac.birthYear = ?",
                    ["groupId", "birthYear"]]
    ],

    $DSL.select {
        TARGET (Actor.alias("ac"))
        WHERE {
            LT (ac.age, ALL(QUERY {
                TARGET (Ages.alias("ag"))
                FETCH (ag.age)
            }))
        }
    },
    [
            mysql: "SELECT * FROM `Actor` ac WHERE ac.age < ALL (SELECT ag.age FROM `Ages` ag)"
    ],
]