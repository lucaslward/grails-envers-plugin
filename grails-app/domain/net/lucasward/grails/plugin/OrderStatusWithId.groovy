package net.lucasward.grails.plugin

enum OrderStatusWithId {
    INITIAL, IN_PROGRESS, COMPLETE

    // IMPORTANT: This cannot be a private field for the workaround in Envers 4.3.6 to work properly.
    final String id

    OrderStatusWithId() {
        this.id = name()
    }
}