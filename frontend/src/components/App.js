import React from 'react'
import { Switch, Route, BrowserRouter } from 'react-router-dom'
import Frame from './mainLayout/Frame'
import Login from './routes/login/Login'
import ProtectedRoute from './routes/ProtectedRoute'
import routes from '../constants/routes'
import theme from '../theme'
import { ThemeProvider } from '@material-ui/styles'

export default () => {

    return (
        <ThemeProvider theme={theme}>
            <BrowserRouter>
                <Switch>
                    <Route exact path={routes.LOGIN} component={Login} />
                    <ProtectedRoute path='/' component={Frame} />
                </Switch>
            </BrowserRouter>
        </ThemeProvider>
    )
}