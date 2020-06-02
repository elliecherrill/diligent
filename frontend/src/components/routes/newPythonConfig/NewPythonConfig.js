import React from 'react'
import {initialPythonConfigs} from '../../../constants/config'
import routes from '../../../constants/routes'
import NewFrame from '../../NewFrame'

class NewPythonConfig extends React.Component {
    render() {
        return (<NewFrame
            inverseChecksSelected={(unusedChecks) => {return false}}
            getInverseChecks={(unusedChecks) => {return []}}
            type={'python'}
            homeRoute={routes.PYTHON}
            initialConfigs={initialPythonConfigs}
        />)
    }
}

export default NewPythonConfig