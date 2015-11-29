cat $1 | sed 's/-LSB-/\[/g' | sed 's/-RSB-/\]/g' | sed 's/-LCB-/{/g' | sed 's/-RCB-/}/g' | sed 's/-AMP-/\&/g' | sed 's/-NUM-/#/g' | sed 's/-GRE-/>/g'  | sed 's/-EQU-/=/g'
