import React from 'react'
import {initialConfigs} from '../../../constants/config'
import routes from '../../../constants/routes'
import NewFrame from '../../NewFrame'

class NewConfig extends React.Component {
    inverseChecksSelected = (unusedChecks) => {
        if (!unusedChecks.includes('config-2') && !unusedChecks.includes('config-3')) {
            return true
        }

        if (!unusedChecks.includes('config-4') && !unusedChecks.includes('config-5')) {
            return true
        }

        if (!unusedChecks.includes('config-6') && !unusedChecks.includes('config-7')) {
            return true
        }

        if (!unusedChecks.includes('config-8') && !unusedChecks.includes('config-9')) {
            return true
        }

        if (!unusedChecks.includes('config-10') && !unusedChecks.includes('config-11')) {
            return true
        }

        return false
    }

    getInverseChecks = (unusedChecks) => {
        if (!unusedChecks.includes('config-2') && !unusedChecks.includes('config-3')) {
            return [initialConfigs.configs['config-2'].content, initialConfigs.configs['config-3'].content]
        }

        if (!unusedChecks.includes('config-4') && !unusedChecks.includes('config-5')) {
            return [initialConfigs.configs['config-4'].content, initialConfigs.configs['config-5'].content]
        }

        if (!unusedChecks.includes('config-6') && !unusedChecks.includes('config-7')) {
            return [initialConfigs.configs['config-6'].content, initialConfigs.configs['config-7'].content]
        }

        if (!unusedChecks.includes('config-8') && !unusedChecks.includes('config-9')) {
            return [initialConfigs.configs['config-8'].content, initialConfigs.configs['config-9'].content]
        }

        if (!unusedChecks.includes('config-10') && !unusedChecks.includes('config-11')) {
            return [initialConfigs.configs['config-10'].content, initialConfigs.configs['config-11'].content]
        }

        return []
    }

    render() {
        return (<NewFrame
            inverseChecksSelected={(unusedChecks) => this.inverseChecksSelected(unusedChecks)}
            getInverseChecks={(unusedChecks) => this.getInverseChecks(unusedChecks)}
            type={'java'}
            homeRoute={routes.HOME}
            initialConfigs={initialConfigs}
        />)
    }
}

export default NewConfig