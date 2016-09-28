# 11PathsPlayModule

import getopt
from play.utils import *

MODULE = '11paths'

# Commands that are specific to your module

COMMANDS = ["11paths:", "11paths:ov", "11paths:override"]

HELP = {
    "11paths:": "Show help for the 11PathsPlayModule",
    "11paths:override": "Override the content of module"
}

def execute(**kargs):
    command = kargs.get("command")
    app = kargs.get("app")
    args = kargs.get("args")
    env = kargs.get("env")

    if command == '11paths:':
        print "~ Use: --css to override the Module css"
        print "~      --admin to override the admin pages (roles and invitations)"
        print "~      --api to override the api clients page"
        print "~      --mailer to override the ElevenPaths mailer"
        print "~      --errors to override error pages"
        print "~      --lang to override language choices"
        print "~      --public to override the public content pages (register, activation and reset password)"
        print "~      --secure to override login page and layout"
        print "~      --users to override users page"
        print "~ "
        return

    try:
        optlist, args2 = getopt.getopt(args, '', ['css', 'admin', 'api', 'mailer', 'errors', 'lang', 'public', 'secure', 'users'])
        for o, a in optlist:
            if o == '--css':
                app.override('public/stylesheets/secure.css', 'public/stylesheets/secure.css')
                app.override('public/stylesheets/errors.css', 'public/stylesheets/errors.css')
                print "~ "
                return
            if o == '--admin':
                app.override('app/views/Admin/invitations.html', 'app/views/Admin/invitations.html')
                app.override('app/views/Admin/roles.html', 'app/views/Admin/roles.html')
                print "~ "
                return
            if o == '--api':
                app.override('app/views/APIClients/index.html', 'app/views/APIClients/index.html')
                print "~ "
                return
            if o == '--mailer':
                app.override('app/views/DarwinMailer/en/activateAccount.html', 'app/views/DarwinMailer/en/activateAccount.html')
                app.override('app/views/DarwinMailer/es/activateAccount.html', 'app/views/DarwinMailer/es/activateAccount.html')

                app.override('app/views/DarwinMailer/en/adminActivateAccount.html', 'app/views/DarwinMailer/en/adminActivateAccount.html')
                app.override('app/views/DarwinMailer/es/adminActivateAccount.html', 'app/views/DarwinMailer/es/adminActivateAccount.html')

                app.override('app/views/DarwinMailer/en/inviteAccount.html', 'app/views/DarwinMailer/en/inviteAccount.html')
                app.override('app/views/DarwinMailer/es/inviteAccount.html', 'app/views/DarwinMailer/es/inviteAccount.html')

                app.override('app/views/DarwinMailer/en/passwordReset.html', 'app/views/DarwinMailer/en/passwordReset.html')
                app.override('app/views/DarwinMailer/es/passwordReset.html', 'app/views/DarwinMailer/es/passwordReset.html')
                print "~ "
                return
            if o == '--errors':
                app.override('app/views/errors/403.html', 'app/views/errors/403.html')
                app.override('app/views/errors/404.html', 'app/views/errors/404.html')
                app.override('app/views/errors/500.html', 'app/views/errors/500.html')
                app.override('app/views/errors/layoutError.html', 'app/views/errors/layoutError.html')
                print "~ "
                return
            if o == '--lang':
                app.override('app/views/Lang/languages.html', 'app/views/Lang/languages.html')
                print "~ "
                return
            if o == '--public':
                app.override('app/views/PublicContentBase/activate.html', 'app/views/PublicContentBase/activate.html')
                app.override('app/views/PublicContentBase/passwordReset.html', 'app/views/PublicContentBase/passwordReset.html')
                app.override('app/views/PublicContentBase/processRequestPasswordReset.html', 'app/views/PublicContentBase/processRequestPasswordReset.html')
                app.override('app/views/PublicContentBase/register.html', 'app/views/PublicContentBase/register.html')
                app.override('app/views/PublicContentBase/requestPasswordReset.html', 'app/views/PublicContentBase/requestPasswordReset.html')
                print "~ "
                return
            if o == '--secure':
                app.override('app/views/Secure/layout.html', 'app/views/Secure/layout.html')
                app.override('app/views/Secure/login.html', 'app/views/Secure/login.html')
                print "~ "
                return
            if o == '--users':
                app.override('app/views/Users/index.html', 'app/views/Users/index.html')
                print "~ "
                return

    except getopt.GetoptError, err:
        print "~ %s" % str(err)
        print "~ "
        sys.exit(-1)

