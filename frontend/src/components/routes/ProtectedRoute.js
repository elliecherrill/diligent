import React from 'react'
import { Redirect, Route } from 'react-router-dom'
import authentication from '../../utils/authenticationService'
import routes from '../../constants/routes'

export default function ProtectedRoute({component: Component, ...rest}) {
    return (
        <Route {...rest} render={props =>
            authentication.userIsLoggedIn()
                ? (<Component {...props} />)
                : (<Redirect to={{
                    pathname: routes.LOGIN,
                    state: {from: props.location}
                }}/>)
        }/>
    )
}