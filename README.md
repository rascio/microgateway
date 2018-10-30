# TODO

## Handlers
- The handlers contract should be `Req->Publisher<R>` instead of `Req->R`
- Handlers should accesss the body as `Mono`/`Flux`
    
## Request Serialization
- Handle deserialization errors (bad json, bad types, etc...)
- Wrap requests and responses in class like:
    ```
    class Envelope<H, R extends Request<?, ?, ?, ?> {    
        H headers;
        R request;
    }
    ```
    and manage HTTP headers
- Manage query parameters for all HTTP methods
- `java.time` serialization doesn't work

## Hypermedia
- Add an `AuthorizationGateway` like:
    ```
    interface AuthorizationGateway {
        boolean isAuthorized(Envelope/Request)
    }
    ```
- Create Jackson `@Filter` to filter not authorized `Request` fields from response