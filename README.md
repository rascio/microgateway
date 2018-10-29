# TODO

## Modules
- Add a new `Gateway` type to be used in `Module#register` to give dependencies an instance of `Gateway` before it is ready (a stateful implementation, rejecting request when not ready).
    
## Request Serialization
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