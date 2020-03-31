import authConstants from '../constants/auth'

function storeTokens({ access_token }) {
    sessionStorage.setItem(authConstants.ACCESS_TOKEN, access_token)
    // sessionStorage.setItem(authConstants.REFRESH_TOKEN, refresh_token)
}

function removeTokens() {
    sessionStorage.removeItem(authConstants.ACCESS_TOKEN)
    // sessionStorage.removeItem(authConstants.REFRESH_TOKEN)
}

async function login(username, password) {
    const requestOptions = {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
    }

    const response = await fetch('/api/login', requestOptions)
    if (response.ok) {
        const data = await response.json()
        storeTokens(data)
        return true
    }
    return false
}

const logout = () => removeTokens()
const userIsLoggedIn = () => {
    return sessionStorage.getItem(authConstants.ACCESS_TOKEN) !== null
}

export default {
    login,
    logout,
    userIsLoggedIn
}