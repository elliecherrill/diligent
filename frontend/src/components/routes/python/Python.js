import {
    Grid,
    CircularProgress,
} from '@material-ui/core'
import React from 'react'
import colours from '../../../constants/colours'

//TODO: to finish
const Python = (props) => {
    document.body.style.backgroundColor = colours.PRIMARY
    props.setGoToPython(false)

    // const [loaded, setLoaded] = useState(false)

    // if (!loaded) {
        return (<div>
            <Grid container justify='center'>
                <Grid item>
                    <CircularProgress color={'secondary'}/>
                </Grid>
            </Grid>
        </div>)
    // }

    // return (
    //     <div>
    //     </div>
    // )
}


export default Python