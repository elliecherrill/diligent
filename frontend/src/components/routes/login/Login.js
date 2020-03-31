import React, { useState } from 'react'
import { Redirect } from 'react-router-dom'
import {
    Button,
    CssBaseline,
    TextField,
    Typography,
    Container
} from '@material-ui/core/index'
import useStyles from './style'
import authentication from '../../../utils/authenticationService'
import routes from '../../../constants/routes'

export default props => {
    const classes = useStyles()
    const [username, setUser] = useState('')
    const [password, setPassword] = useState('')
    const [error, setError] = useState(false)
    const [allowRedirection, setAllowRedirection] = useState(false)
    let { from } = props.location.state || { from: { pathname: routes.HOME } }

    const login = async event => {
        event.preventDefault()
        const success = await authentication.login(username, password)
        if (success) setAllowRedirection(success)
        else {
            setUser('')
            setPassword('')
            setError(!success)
        }
    }

    if (allowRedirection) return <Redirect to={from} />
    return (
        <Container component='main' maxWidth='xs'>
            <CssBaseline />
            <div className={classes.paper}>
                <Typography component='h1' variant='h3' color='primary'>
                    Diligent
                </Typography>
                <form className={classes.form} onSubmit={login}>
                    <TextField
                        error={error}
                        variant='outlined'
                        margin='normal'
                        value={username}
                        required
                        fullWidth
                        id='email'
                        label='Username'
                        name='email'
                        onChange={event => setUser(event.target.value)}
                        autoFocus
                    />
                    <TextField
                        error={error}
                        variant='outlined'
                        margin='normal'
                        required
                        fullWidth
                        value={password}
                        name='password'
                        label='Password'
                        type='password'
                        onChange={event => setPassword(event.target.value)}
                    />
                    <Button
                        type='submit'
                        fullWidth
                        variant='contained'
                        color='primary'
                        className={classes.submit}>
                        Log In
                    </Button>
                </form>
            </div>
            <Container hidden={!error}>
                <Typography variant='body2' align='center' color='error'>
                    Invalid Credentials. Try again.
                </Typography>
            </Container>
        </Container>
    )
}
