# TODO

## Handlers
- The handlers contract should be `Req->Publisher<R>` instead of `Req->R`
- Add `HandlerWrapper`s registration in `DefaultGateway.Builder` like: 
    ```
    interface HandlerWrapper {
        <T extends Request<?, ?, ?, R>, R> wrap(Class<T> type, Function<Envelope<T>, R> handler);
    }
    ```
    
## Requests
- Handle deserialization errors server side (bad json, bad types, etc...)
- Handle error responses in client
- Manage query parameters for all HTTP methods

## Hypermedia
- Cache inspection and request filtering strategies @see AuthorizationGateway
- Jackson `Request` deserialization with `RestApiRegistry`