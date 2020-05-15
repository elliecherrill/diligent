import {
    Grid,
    CircularProgress,
} from '@material-ui/core'
import React, {useState} from 'react'
import colours from '../../../constants/colours'

const Python = (props) => {
    document.body.style.backgroundColor = colours.PRIMARY
    props.setGoToPython(false)

    const [loaded, setLoaded] = useState(false)

    if (!loaded) {
        return (<div>
            <Grid container justify='center'>
                <Grid item>
                    <CircularProgress color={'secondary'}/>
                </Grid>
            </Grid>
        </div>)
    }

    return (
        <div>
        </div>
    )
}


export default Python