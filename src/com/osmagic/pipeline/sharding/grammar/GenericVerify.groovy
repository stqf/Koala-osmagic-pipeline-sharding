package com.osmagic.pipeline.sharding.grammar

class GenericVerify {

    static void main(String[] args) {
        def setItems = new HashSet<>()
        setItems.add("123")
        setItems.add("123")
        setItems.add("456")
        println(setItems)
    }

}
