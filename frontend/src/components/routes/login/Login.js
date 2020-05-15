import React, {useState} from 'react'
import {Redirect} from 'react-router-dom'
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
import colours from '../../../constants/colours'
import theme from '../../../theme'
import logo from '../../../images/diligent.svg'

export default props => {
    document.body.style.backgroundColor = 'inherit'
    const classes = useStyles()
    const [username, setUsername] = useState('')
    const [password, setPassword] = useState('')
    const [error, setError] = useState(false)
    const [allowRedirection, setAllowRedirection] = useState(false)
    let {from} = props.location.state || {from: {pathname: routes.HOME}}

    const login = async event => {
        event.preventDefault()
        const success = await authentication.login(username, password)
        if (success) {
            setAllowRedirection(success)
        } else {
            setUsername('')
            setPassword('')
            setError(true)
        }
    }

    if (allowRedirection) return <Redirect push to={from}/>
    return (

        <Container component='main'>
            <CssBaseline/>
            <div className={classes.paper}>
                <div className={classes.loginContainer}>
                    <div className='hero-body' style={{width: '100%'}}>
                        <div className='container'>
                            <div style={{display: 'flex', flexDirection: 'row', alignItems: 'center', justifyContent: 'center'}}>
                                <img src={logo} alt={'Diligent logo'} style={{width: '10%', marginRight: '5%', marginBottom: '5%'}}/>
                                <h1 className='title is-1'
                                    style={{fontSize: '4rem', textAlign: 'center', color: colours.PRIMARY, marginTop: '5%'}}>
                                    Diligent
                                </h1>
                            </div>
                            <h1 className='subtitle is-3'
                                style={{
                                    fontSize: '2rem',
                                    textAlign: 'center',
                                    color: theme.palette.primary.light,
                                    marginBottom: '5%',
                                    marginLeft: '5%'
                                }}>
                                Improving your coding practices
                            </h1>
                        </div>
                    </div>
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
                            onChange={event => {
                                setError(false)
                                setUsername(event.target.value)
                            }}
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
                            onChange={event => {
                                setError(false)
                                setPassword(event.target.value)
                            }
                            }
                        />
                        <Button
                            type='submit'
                            fullWidth
                            variant='contained'
                            color='primary'
                            className={classes.submit}>
                            Log In
                        </Button>
                        {error &&
                        <Container>
                            <Typography variant='body2' align='center' color='error'>
                                Invalid Credentials. Try again.
                            </Typography>
                        </Container>
                        }
                    </form>
                </div>
            </div>

        </Container>
    )
}
