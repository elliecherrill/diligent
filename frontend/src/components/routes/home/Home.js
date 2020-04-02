import {
    Button
} from '@material-ui/core'
import React, {useState} from 'react'
import colours from '../../../constants/colours'
import {Redirect} from 'react-router-dom'
import routes from '../../../constants/routes'
import NewConfigSnackbar from "./NewConfigSnackbar";

const Home = (props) => {
    document.body.style.backgroundColor = colours.PRIMARY
    const [goToNewConfig, setGoToNewConfig] = useState(false)


    return (
        <div>
            <section className='hero is-large'>
                <div className='hero-body'>
                    <div className='container'>
                        <h1 className='title is-1' style={{fontSize: '5rem', textAlign: 'center', color: 'white'}}>
                            Diligent
                        </h1>
                        <h1 className='subtitle is-3'
                            style={{
                                fontSize: '2rem',
                                textAlign: 'center',
                                color: 'rgb(220, 220, 220)',
                                marginBottom: '5%'
                            }}>
                            A tool which does something.
                        </h1>
                    </div>
                </div>
            </section>
            <div style={{display: 'flex', justifyContent: 'center'}}>
                <Button
                    variant='outlined'
                    color='secondary'
                    onClick={() => setGoToNewConfig(true)}
                >
                    Create A New Configuration
                </Button>
            </div>

            {goToNewConfig ? <Redirect to={routes.NEW_CONFIG}/> : false}

            {props.location.state ? <NewConfigSnackbar title={props.location.state} /> : false}
        }
        </div>
    )
}


export default Home