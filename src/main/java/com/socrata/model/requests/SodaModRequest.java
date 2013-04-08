package com.socrata.model.requests;

/**
 * THis is a SODA request that is meant to modify some object, so it
 * has all the normal SodaRequest members, but also contains an
 * id of the object being modified.
 */
abstract public class SodaModRequest<T> extends SodaRequest<T>
{
    final public Object id;

    public SodaModRequest(String resourceId, T payload, Object id)
    {
        super(resourceId, payload);
        this.id = id;
    }
}
